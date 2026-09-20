# 🚀 Nhật ký Phát triển: Ticketing Engine

## Giai đoạn 1: Khởi tạo & Cấu hình Hạ tầng
*   Push thành công bộ khung project Spring Boot nguyên bản lên kho lưu trữ GitHub.
*   Thiết lập hệ thống quản trị dự án Kanban Board với 4 cột chuẩn mực (`To Do`, `In Progress`, `Review / Testing`, `Done`).
*   Tích hợp và chạy thành công Docker Compose cho hệ thống PostgreSQL, Redis và RabbitMQ.
*   Cấu hình an toàn tệp `application.properties` bằng kỹ thuật Biến môi trường (Environment Variables) để bảo mật thông tin kết nối.

## Giai đoạn 2: Thiết kế & Xây dựng Core Data
*   Định hình Issue 1 và chốt bản thiết kế Database Schema tối ưu hóa cho hệ thống phân tán.
*   Xây dựng cấu trúc Redis Keys chuyên dụng để chịu tải cao (Stock Key, Idempotency Key, Rate Limit Key).
*   Tài liệu hóa toàn bộ bản thiết kế vào tệp `docs/database-design.md` và gộp vào nhánh chính.
*   Tích hợp thư viện Lombok để tối ưu hóa mã nguồn Java.
*   Lập trình hoàn thiện 4 Entity cốt lõi (`Event`, `Ticket` - Optimistic Locking, `Order` - UUID, `User`) và các Repository tương ứng.

## Giai đoạn 3: Xử lý Concurrency với Redis (Issue 3)
*   **Redis Config:** Tích hợp thành công `spring-boot-starter-data-redis` và gộp chung cấu hình `RedissonClient` (Distributed Lock) cùng `RedisTemplate` vào file `config/RedisConfig.java`.
*   **Lua Script:** Viết và nạp thành công script `decrement_stock.lua` đảm bảo tính nguyên tử (atomic) khi trừ tồn kho, loại bỏ hoàn toàn rủi ro bán âm vé (overselling).
*   **Tầng Service & Controller:** Xây dựng `TicketService` thực thi Lua Script và mở API endpoint `POST /api/tickets/{id}/reserve`.
*   **Security:** Xây dựng `SecurityConfig.java` cấu hình tắt CSRF và mở quyền truy cập API để thuận tiện cho việc kiểm thử hiệu năng.
*   **Testing:** Khởi tạo dữ liệu vé giả lập trên Redis CLI qua Docker và dùng Postman gọi API trừ vé thành công.

## Giai đoạn 4: Xác thực & Phân quyền (Issue 4)
*   **Dependencies:** Tích hợp Spring Security, Validation và thư viện JJWT (bản 0.12.5 mới nhất).
*   **Core Entity:** Xây dựng `User` Entity implements `UserDetails`, kết nối PostgreSQL qua `UserRepository`.
*   **Security Configuration:** Áp dụng kiến trúc Constructor Injection cho `DaoAuthenticationProvider`, sử dụng thuật toán băm mật khẩu `BCrypt`.
*   **JWT Filter:** Triển khai `JwtAuthenticationFilter` (`OncePerRequestFilter`) để kiểm tra Bearer Token ở cấp độ HTTP Header.
*   **API Security:** Cấu hình SecurityFilterChain để mở khóa tự do cho `/api/auth/**` và khóa toàn bộ các API nghiệp vụ (`/api/tickets/**`).
*   **Testing:** Hoàn tất luồng kiểm thử Postman (Đăng ký -> Đăng nhập lấy Token -> Gọi API Mua vé thành công).
   
## Giai đoạn 5: Xử lý Bất đồng bộ với RabbitMQ (Issue 5)
*   **Message Broker Config:** Tích hợp `spring-boot-starter-amqp` và cấu hình thành công Exchange, Queue, Binding trong `RabbitMQConfig.java`.
*   **Message Converter:** Áp dụng `Jackson2JsonMessageConverter` để serialize dữ liệu thành định dạng JSON dễ đọc, tối ưu cho việc debug.
*   **Data Transfer Object:** Sử dụng Java `record` (`OrderMessage`) để định nghĩa DTO mang dữ liệu an toàn và ngắn gọn.
*   **Asynchronous Flow:** Tái cấu trúc `TicketService`, trích xuất thông tin User từ `SecurityContext` và đẩy message vào Queue thông qua `RabbitTemplate` ngay sau khi Redis trừ vé thành công, đảm bảo API phản hồi cực nhanh (Low Latency).
   
## Giai đoạn 6: Xây dựng Worker xử lý Đơn hàng (Issue 6)
*   **Database Repository:** Khởi tạo `OrderRepository` để tương tác với bảng `orders`.
*   **Business Logic:** Xây dựng `OrderService` chịu trách nhiệm lưu đơn hàng. Tối ưu hóa hiệu suất bằng cách map trực tiếp `userId` và `ticketId` từ Message thay vì query object.
*   **Message Listener:** Triển khai `OrderMessageListener` sử dụng `@RabbitListener` túc trực tại `order.queue`, hứng message JSON và gọi Service lưu xuống PostgreSQL.
*   **Entity Optimization:** Tích hợp `@CreationTimestamp` của Hibernate để tự động hóa việc ghi nhận thời gian tạo đơn hàng (Audit Time) chuẩn xác.
   
## Giai đoạn 7: Xử lý Lỗi & Đảm bảo Toàn vẹn Dữ liệu (Issue 7)
*   **Retry Mechanism:** Kích hoạt cấu hình `spring.rabbitmq.listener.simple.retry` trong `application.properties`, thiết lập cơ chế tự động thử lại 3 lần với backoff multiplier nhằm chống ngập lụt lỗi tạm thời.
*   **Dead Letter Queue (DLQ):** Xây dựng cấu trúc DLX (`order.dlx`) và DLQ (`order.dlq`) trong `RabbitMQConfig`.
*   **Zero Data Loss:** Tích hợp DLX vào `order.queue` bằng `QueueBuilder`, đảm bảo các thông điệp đặt vé không thể xử lý sẽ được lưu trữ an toàn để đối soát và bồi hoàn sau này.

## Giai đoạn 8: Giao dịch Bù trừ - Hoàn vé tự động (Issue 8)
*   **Saga Pattern (Compensating Transaction):** Triển khai Worker thứ hai (`RefundMessageListener`) chuyên biệt để lắng nghe các đơn hàng lỗi từ Dead Letter Queue (`order.dlq`).
*   **Redis Lua Script:** Viết và nạp kịch bản `increment_stock.lua` đảm bảo tính nguyên tử (atomic) khi hoàn vé.
*   **Zero Data Loss & No Overselling:** Ngăn chặn triệt để tình trạng "thất thoát vé" bằng cách cộng trả lại (+quantity) số vé vào Redis ngay khi phát hiện giao dịch lưu Database thất bại.
---
*Ghi chú: File này được tạo ra nhằm mục đích theo dõi tiến độ và cung cấp bối cảnh (context) cho các phiên làm việc tiếp theo.*
