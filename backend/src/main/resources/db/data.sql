-- ============================================================
-- 演示数据（可选，由管理员账号执行）
-- 全部为虚构姓名/地址，手机号中间四位以 x 代替，不含真实个人信息。
-- 使用 NOT EXISTS 保证可重复执行。
-- ============================================================

USE express_address_book;

INSERT INTO express_address (receiver_name, phone, full_address)
SELECT '菜小鸟', '136xxxx0000', '浙江省杭州市西湖区湖滨小区12栋301室'
WHERE NOT EXISTS (SELECT 1 FROM express_address WHERE phone = '136xxxx0000');

INSERT INTO express_address (receiver_name, phone, full_address)
SELECT '菜中鸟', '136xxxx0001', '湖北省武汉市江岸区沿江小区8栋602室'
WHERE NOT EXISTS (SELECT 1 FROM express_address WHERE phone = '136xxxx0001');

INSERT INTO express_address (receiver_name, phone, full_address)
SELECT '菜大鸟', '136xxxx0002', '湖北省孝感市孝南区东苑小区15栋201室'
WHERE NOT EXISTS (SELECT 1 FROM express_address WHERE phone = '136xxxx0002');
