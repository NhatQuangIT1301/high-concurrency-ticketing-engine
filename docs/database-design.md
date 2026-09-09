# *Database Design & Cấu trúc Redis*

## 1. PostgreSQL Schema (Cơ sở dữ liệu chính)

### Bảng: `events` (Sự kiện)
Lưu thông tin chung về sự kiện (vd: "Concert Wuthering Waves 2026").
* **`id`** (`BIGINT`, **PK**): ID sự kiện.
* **`name`** (`VARCHAR`): Tên sự kiện.
* **`start_time`** (`TIMESTAMP`): Thời gian bắt đầu mở bán.
* **`end_time`** (`TIMESTAMP`): Thời gian kết thúc.
* **`status`** (`VARCHAR`): Trạng thái (`UPCOMING`, `ONGOING`, `ENDED`).

---

### Bảng: `tickets` (Thông tin Vé/Kho)
Lưu trữ số lượng và giá vé.
* **`id`** (`BIGINT`, **PK**): ID loại vé.
* **`event_id`** (`BIGINT`, **FK**): Liên kết với bảng `events`.
* **`ticket_type`** (`VARCHAR`): Loại vé (VIP, VVIP, Standard).
* **`price`** (`DECIMAL`): Giá vé.
* **`total_stock`** (`INT`): Tổng số lượng vé phát hành.
* **`available_stock`** (`INT`): Số lượng vé còn lại trong kho thực tế.
* **`version`** (`INT`): Dùng cho cơ chế Optimistic Locking ở tầng DB để backup rủi ro data bị ghi đè.

---

### Bảng: `orders` (Đơn hàng)
* **`id`** (`UUID`, **PK**): Mã đơn hàng (vd: `550e8400-e29b-41d4...`).
* **`user_id`** (`BIGINT`): ID của người mua.
* **`ticket_id`** (`BIGINT`, **FK**): Mua loại vé nào.
* **`quantity`** (`INT`): Số lượng mua (thường giới hạn 1-2 vé/người).
* **`status`** (`VARCHAR`): Trạng thái State Machine (`PENDING` -> `PAID` hoặc `CANCELLED`).
* **`created_at`** (`TIMESTAMP`): Thời gian tạo (Đánh Index để Worker quét các đơn quá hạn 10 phút).

---

## 2. Cấu trúc Key trên Redis (Tâm điểm chịu tải)

* **Key Tồn Kho (Stock Key):**
  * Định dạng: `ticket:{ticket_id}:stock` (`String/Integer`).
  * Ví dụ: `ticket:101:stock = 500`.

* **Key Chống Trùng Lặp (Idempotency Key):**
  * Định dạng: `order:idempotent:{user_id}:{ticket_id}` (`String`).
  * Ví dụ: `order:idempotent:999:101 = "processing"`.

* **Key Giới hạn Request (Rate Limiter Key):**
  * Định dạng: `rate_limit:{user_id}` (`String/Integer`).