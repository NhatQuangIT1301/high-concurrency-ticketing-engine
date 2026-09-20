-- KEYS[1]: Redis Key của kho vé (vd: ticket:1:stock)
-- ARGV[1]: Số lượng vé muốn hoàn (vd: 1)

local stock_key = KEYS[1];
local quantity = tonumber(ARGV[1]);

local current_stock = redis.call('GET', stock_key);

-- Trường hợp 1: Key chưa được setup trên Redis (Lỗi dữ liệu)
if not current_stock then
    return -1
end

-- Trường hợp 2: Cộng trả lại số vé vào kho
redis.call('INCRBY', stock_key, quantity)
return 1