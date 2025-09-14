import app from './app';
import { connectDB } from './config/db';

const PORT = process.env.PORT || 3000;

import { connectRabbitMQ } from './messaging/rabbitmq';
import { orderCanceledConsumer, orderCreatedConsumer, orderUpdatedConsumer, orderItemAddedConsumer, orderDeletedConsumer } from './messaging/consumer';

(async () => {
  await connectRabbitMQ();

  await orderCreatedConsumer();
  await orderUpdatedConsumer();
  await orderCanceledConsumer();
  await orderItemAddedConsumer();
  await orderDeletedConsumer();
})();

connectDB().then(() => {
  app.listen(PORT, () => {
    console.log(`🚀 Product Service running at http://localhost:${PORT}`);
  });
});
