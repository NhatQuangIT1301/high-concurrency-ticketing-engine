# 📚 Tài Liệu Tham Khảo API (API Reference)

Tài liệu này cung cấp chi tiết về các endpoint của hệ thống **High Concurrency Ticketing Engine**.

## 🔒 Xác Thực (Authentication)
Hệ thống sử dụng **JWT (JSON Web Token)** để bảo mật. 
* Sau khi gọi API `/api/auth/login` hoặc `/api/auth/register`, bạn sẽ nhận được `token`.
* Truyền token vào Header: `Authorization: Bearer `

## 1. Authentication (Xác thực)

### 1.1. Đăng ký tài khoản
* **Endpoint:** `POST /api/auth/register`
* **Request Body (JSON):**
    ```json
    {
        "username": "nguyenvan_a",
        "email": "nguyenvana@example.com",
        "password": "password123"
    }
    ```
* **Response (200 OK):**
    ```json
    {
        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6...",
        "message": "Đăng ký thành công"
    }
    ```

### 1.2. Đăng nhập
* **Endpoint:** `POST /api/auth/login`
* **Request Body (JSON):**
    ```json
    {
        "email": "nguyenvana@example.com",
        "password": "password123"
    }
    ```
* **Response (200 OK):**
    ```json
    {
        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6...",
        "message": "Đăng nhập thành công"
    }
    ```

## 2. Tickets (Quản lý Vé)

### 2.1. Đặt mua vé
* **Mô tả:** Đặt mua vé cho một sự kiện. Luồng này sử dụng Redis Lua Script để trừ vé nguyên tử, tránh Overselling.
* **Endpoint:** `POST /api/tickets/{ticketId}/reserve`
* **Xác thực:** Có (Bearer Token)
* **Query:** `quantity` (int, mặc định 1)
* **Ví dụ:** `POST /api/tickets/1/reserve?quantity=2`
* **Response (200 OK):** `Mua thành công 2 vé cho Ticket Id: 1`
* **Response Lỗi (400):** `Rất tiếc, vé đã sold out!`

## 3. Orders (Quản lý Đơn hàng)

### 3.1. Lấy danh sách đơn hàng
* **Mô tả:** Lấy danh sách toàn bộ đơn hàng do User hiện tại đã đặt (sắp xếp mới nhất lên đầu). Truy vấn được tối ưu tốc độ thông qua Redis Cache.
* **Endpoint:** `GET /api/orders/my-orders`
* **Xác thực:** Có (Bearer Token)
* **Response (200 OK):**
    ```json
    [
        {
            "id": "75cd8621-6205-48b7-ac81-964e4cd42202",
            "userId": 1,
            "ticketId": 1,
            "quantity": 2,
            "status": "PENDING",
            "createdAt": "2026-09-27T15:00:00"
        },
        {
            "id": "8f3b2313-9128-...",
            "userId": 1,
            "ticketId": 2,
            "quantity": 1,
            "status": "CANCELED",
            "createdAt": "2026-09-26T10:00:00"
        }
    ]
    ```

### 3.2. Thanh toán đơn hàng (Mock)
* **Mô tả:** Giả lập thao tác thanh toán thành công, chuyển đổi trạng thái đơn hàng từ `PENDING` sang `PAID` và cập nhật lại Cache. Chặn cơ chế tự động hủy (Timeout Worker) của hệ thống.
* **Endpoint:** `POST /api/orders/{orderId}/pay`
* **Xác thực:** Có (Bearer Token)
* **Response (200 OK):** `Thanh toán thành công đơn hàng 75cd...`
* **Response Lỗi (400 Bad Request):** `Không có quyền truy cập đơn hàng này!`