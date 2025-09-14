import { ConsumeMessage } from 'amqplib';
import { getChannel } from './rabbitmq';
import { handleOrderCanceled, handleOrderCreated, handleOrderDeleted, handleOrderItemAdded, handleOrderUpdated } from '../services/orderHandler';

export const orderCreatedConsumer = async (): Promise<void> => {
  const channel = await getChannel();
  const queue = 'order.craeted.queue';

  channel.consume(queue, async(msg: ConsumeMessage | null) => {
    if (!msg) return;
    try {
    const data = JSON.parse(msg.content.toString());
    console.log(`[←] Received message:`, data);

    await handleOrderCreated(data); // <- if async
    channel.ack(msg);
  } catch (err) {
    console.error('❌ Failed to process message', err);
    channel.nack(msg, false, false);
  }
  });

  console.log('[✓] Order consumer started and listening for order.created');
};

export const orderUpdatedConsumer = async (): Promise<void> => {
  const channel = await getChannel();
  const queue = 'order.updated.queue';

  channel.consume(queue, async(msg: ConsumeMessage | null) => {
    if (!msg) return;
    try {
    const data = JSON.parse(msg.content.toString());
    console.log(`[←] Received message:`, data);

    await handleOrderUpdated(data); 
    channel.ack(msg);
  } catch (err) {
    console.error('❌ Failed to process message', err);
    channel.nack(msg, false, false);
  }
  });

  console.log('[✓] Order consumer started and listening for order.updated');
}

export const orderCanceledConsumer = async (): Promise<void> => {
  const channel = await getChannel();
  const queue = 'oreder.canceled.queue';

  channel.consume(queue, async(msg: ConsumeMessage | null) => {
    if (!msg) return;
    try {
    const data = JSON.parse(msg.content.toString());
    console.log(`[←] Received message:`, data);

    await handleOrderCanceled(data); 
    channel.ack(msg);
  } catch (err) {
    console.error('❌ Failed to process message', err);
    channel.nack(msg, false, false);
  }
  });

  console.log('[✓] Order consumer started and listening for order.canceled');
}

export const orderItemAddedConsumer = async (): Promise<void> => {
  const channel = await getChannel();
  const queue = 'orderItem.added.queue';

  channel.consume(queue, async(msg: ConsumeMessage | null) => {
    if (!msg) return;
    try {
    const data = JSON.parse(msg.content.toString());
    console.log(`[←] Received message:`, data);

    await handleOrderItemAdded(data); 
    channel.ack(msg);
  } catch (err) {
    console.error('❌ Failed to process message', err);
    channel.nack(msg, false, false);
  }
  });

  console.log('[✓] Order consumer started and listening for orderItem.added');
}

export const orderDeletedConsumer = async(): Promise<void> => {
  const channel = await getChannel();
  const queue = 'order.deleted.queue';

  channel.consume(queue, async(msg: ConsumeMessage | null) => {
    if (!msg) return;
    try {
    const data = JSON.parse(msg.content.toString());
    console.log(`[←] Received message:`, data);

    await handleOrderDeleted(data); 
    channel.ack(msg);
  } catch (err) {
    console.error('❌ Failed to process message', err);
    channel.nack(msg, false, false);
  }
  });

  console.log('[✓] Order consumer started and listening for orderItem.added');
}