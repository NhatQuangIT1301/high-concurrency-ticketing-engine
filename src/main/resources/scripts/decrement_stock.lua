-- KEYS[1]: Redis Key của kho vé (vd: ticket:101:stock)
-- ARGV[1]: Số lượng vé muốn mua (vd: 1)

local stock_key = KEYS[1]
local quantity = tonumber(ARGV[1])

local current_stock = redis.call('GET', stock_key)

-- Trường hợp 1: Key chưa được setup trên Redis (Chưa nạp kho)
if not current_stock then
    return -1
end

-- Trường hợp 2: Đủ số lượng vé -> Trừ kho và trả về 1
if tonumber(current_stock) >= quantity then
    redis.call('DECRBY', stock_key, quantity)
    return 1
-- Trường hợp 3: Không đủ vé (Sold out) -> Trả về 0
else
    return 0
end