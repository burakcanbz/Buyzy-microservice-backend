import { Nullable } from './../types/nullable.types';
import { HydratedDocument } from 'mongoose';
import { getProductById, updateProduct } from '../services/product.service';
import logger from '../utils/logger';
import { Product } from '../types/product.types';

interface OrderItem {
    id: number;
    productId: string;
    quantity: number;
    order: any;
}

interface Order {
    id: number;
    userId: string;
    status: string;
    items: OrderItem[];
}

interface OrderDTO {
  id: number;
  userId: string;
  status: string;
  existingItems: OrderItem[];
  updatedItems: OrderItem[];
}

export const handleOrderCreated = async (order: Order): Promise<void> => {
    try{
        for (const item of order.items) {
          const product = await getProductById(item.productId);
          if (!product) {
            logger.error(`Product not found: ${item.productId}`);
            throw new Error(`Product not found: ${item.productId}`);
          }
          if (product.stock < item.quantity) {
            logger.error(`Insufficient stock for product ${product.name}`);
            throw new Error(`Insufficient stock for product ${product.name}`);
          }
          const _: Nullable<HydratedDocument<Product>> = await updateProduct(item.productId, { "stock": product.stock - item.quantity});
        }
        logger.info("order created => ", JSON.stringify(order, null, 2));
    }
    catch(err) {
        const error = err as Error;
        logger.error(error.message);
    }
};

export const handleOrderCanceled = async (order: Order): Promise<void> => {
  try{
    for (const item of order.items){
      const product = await getProductById(item.productId);
      if(!product){
        logger.error(`Product not found with id ${item.productId}`);
        throw new Error(`Product not found with id ${item.productId}`);
      }
      if(product.stock < item.quantity) {
        logger.error(`Insufficient stock for product ${product.name}`);
        throw new Error(`Insufficient stock for product ${product.name}`);
      }
      const _: Nullable<HydratedDocument<Product>> = await updateProduct(item.productId, { "stock": product.stock + item.quantity });
    }
    logger.info("product canceled", JSON.stringify(order, null, 2))
  }
  catch(err){
    const error = err as Error;
    logger.error(error.message);
  }
}

export const handleOrderItemAdded = async (orderItem: OrderItem): Promise<void> => {
  try{
      const product = await getProductById(orderItem.productId);
      if(!product){
        logger.error(`Product not found with id ${orderItem.productId}`);
        throw new Error(`Product not found with id ${orderItem.productId}`);
      }
      if(product.stock < orderItem.quantity){
        logger.error(`Insufficient stock for product ${product.name}`);
        throw new Error(`Insufficient stock for product ${product.name}`);
      }
      const _: Nullable<HydratedDocument<Product>> = await updateProduct(orderItem.productId, { "stock": product.stock - orderItem.quantity});
      logger.info("orderItem added", JSON.stringify(orderItem, null, 2));
      }
    catch(err){
      const error = err as Error;
      logger.error(error.message);
    }
}

export const handleOrderUpdated = async(order: OrderDTO): Promise<void> => {
  try {
    for (const updatedItem of order.updatedItems) {
      const existingItem = order.existingItems.find(e => e.productId === updatedItem.productId);
      const diffQuantity = existingItem ? updatedItem.quantity - existingItem.quantity : updatedItem.quantity;
      const product = await getProductById(updatedItem.productId);
      if (!product) throw new Error(`Product not found: ${updatedItem.productId}`);
      const newStock = product.stock - diffQuantity;
      if (newStock < 0) throw new Error(`Insufficient stock for product ${product.name}`);
      await updateProduct(updatedItem.productId, { stock: newStock });
    }
    logger.info("order updated", JSON.stringify(order, null, 2));
  } catch (err) {
    const error = err as Error;
    logger.error(error);
  }
}

export const handleOrderDeleted = async(order: Order): Promise<void> => {
  try{
    for (const item of order.items){
      const product: Nullable<HydratedDocument<Product>> = await getProductById(item.productId);
    if(!product){
        logger.error(`Product not found with id ${item.productId}`);
        throw new Error(`Product not found with id ${item.productId}`);
      }
    if(product.stock < item.quantity){
      logger.error(`Insufficient stock for product ${product.name}`);
      throw new Error(`Insufficient stock for product ${product.name}`);
    }
    const _: Nullable<HydratedDocument<Product>> = await updateProduct(item.productId, { "stock": product.stock + item.quantity});
    }
    logger.info("order deleted =>", JSON.stringify(order, null, 2));
  }
  catch(err){
    const error = err as Error;
    logger.error(error);
  }
}