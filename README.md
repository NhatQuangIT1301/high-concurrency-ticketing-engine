# 🎟️ High Concurrency Ticketing Engine
Một hệ thống backend mô phỏng luồng đặt vé và thanh toán (như mua vé xem phim, vé sự kiện) với khả năng chịu tải cao, giải quyết triệt để các bài toán hóc búa trong hệ thống phân tán (Distributed System).

## 🚀 Các tính năng kỹ thuật nổi bật (Key Features)
Dự án này không chỉ là CRUD thông thường, mà tập trung giải quyết các vấn đề thực tế của hệ thống thương mại điện tử:
* **Ngăn chặn Overselling (Bán lố vé):** Đẩy toàn bộ kho vé lên bộ nhớ đệm. Sử dụng **Redis Lua Script** để đảm bảo tính nguyên tử (Atomic) khi trừ vé, xử lý mượt mà hàng ngàn request cùng lúc.
* **Xử lý Bất đồng bộ (Event-Driven Architecture):** Tách rời luồng xử lý nặng (lưu Database, tạo UUID) ra khỏi luồng chính thông qua **RabbitMQ**, giúp API phản hồi dưới 50ms.
* **Giao dịch Bù trừ (Saga Pattern):** Đảm bảo tính toàn vẹn dữ liệu (Data Consistency). Nếu đơn hàng lưu Database thất bại và rớt vào Dead Letter Queue (DLQ), một Worker sẽ tự động hoàn trả vé (+quantity) lại kho Redis.
* **Payment Timeout (Delayed Messaging):** Tích hợp RabbitMQ Delayed Exchange để tạo "quả bom hẹn giờ". Tự động hủy đơn và hoàn vé nếu người dùng không thanh toán sau 15 phút.
* **Tối ưu Read-Heavy (Caching):** Ứng dụng Spring Cache (`@Cacheable`, `@CacheEvict`) để giảm tải tối đa cho PostgreSQL khi người dùng F5 xem lịch sử mua vé liên tục.

## 🛠️ Tech Stack
* **Framework:** Java 21, Spring Boot 3.x, Spring Data JPA, Spring Cache.
* **Database:** PostgreSQL (Lưu trữ đơn hàng), Redis (Tồn kho, Caching).
* **Message Broker:** RabbitMQ (kèm plugin rabbitmq_delayed_message_exchange).
* **Infrastructure:** Docker, Docker Compose.

## ⚙️ Hướng dẫn cài đặt và chạy (How to run)
1. Clone dự án về máy.
2. Build và khởi động hạ tầng (Postgres, Redis, RabbitMQ) bằng Docker:
   `docker compose up -d --build`
3. Chạy ứng dụng Spring Boot.

## 📝 Tài Liệu & Kiểm Thử API (API Documentation & Testing)
Toàn bộ tài liệu chi tiết về Endpoint, Request/Response Format và cơ chế bảo mật JWT được mô tả tại: 
👉 **[Tài liệu API Reference](docs/API_REFERENCE.md)**

**Chạy thử API với Postman:**
Để thuận tiện cho việc kiểm thử, dự án đã đóng gói sẵn các kịch bản gọi API. Bạn chỉ cần tải file dưới đây và Import trực tiếp vào ứng dụng Postman của mình:
📥 **[Tải xuống Postman Collection](docs/Postman_Collection.json)**

*Lưu ý: Nhớ chạy API Đăng nhập/Đăng ký trước để lấy Token, sau đó gắn vào Header `Authorization: Bearer ` cho các API Đặt vé và Thanh toán.*