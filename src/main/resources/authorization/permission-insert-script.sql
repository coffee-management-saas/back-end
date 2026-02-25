INSERT INTO public.permission (created_at, updated_at, permission_name, permission_description, status)
VALUES
    (NOW(), NOW(), 'employee:create', 'Tạo nhân viên mới', 'ACTIVE'),
    (NOW(), NOW(), 'employee:read', 'Xem danh sách nhân viên', 'ACTIVE'),
    (NOW(), NOW(), 'employee:read-detail', 'Xem chi tiết nhân viên', 'ACTIVE'),
    (NOW(), NOW(), 'employee:update', 'Cập nhật thông tin nhân viên', 'ACTIVE'),
    (NOW(), NOW(), 'employee:delete', 'Xóa nhân viên', 'ACTIVE'),

    (NOW(), NOW(), 'employee-unavailability:create', 'Tạo lịch nghỉ/bận', 'ACTIVE'),
    (NOW(), NOW(), 'employee-unavailability:read', 'Xem danh sách lịch nghỉ', 'ACTIVE'),
    (NOW(), NOW(), 'employee-unavailability:read-detail', 'Xem chi tiết lịch nghỉ', 'ACTIVE'),
    (NOW(), NOW(), 'employee-unavailability:update', 'Cập nhật lịch nghỉ', 'ACTIVE'),
    (NOW(), NOW(), 'employee-unavailability:delete', 'Xóa lịch nghỉ', 'ACTIVE'),

    (NOW(), NOW(), 'schedule:create', 'Tạo lịch làm việc', 'ACTIVE'),
    (NOW(), NOW(), 'schedule:read', 'Xem danh sách lịch làm việc', 'ACTIVE'),
    (NOW(), NOW(), 'schedule:read-detail', 'Xem chi tiết ca làm việc', 'ACTIVE'),
    (NOW(), NOW(), 'schedule:read-by-employee', 'Xem lịch theo nhân viên', 'ACTIVE'),
    (NOW(), NOW(), 'schedule:update', 'Cập nhật lịch làm việc', 'ACTIVE'),
    (NOW(), NOW(), 'schedule:delete', 'Xóa lịch làm việc', 'ACTIVE'),

    (NOW(), NOW(), 'shift-template:create', 'Tạo mẫu ca làm việc', 'ACTIVE'),
    (NOW(), NOW(), 'shift-template:read', 'Xem danh sách mẫu ca', 'ACTIVE'),
    (NOW(), NOW(), 'shift-template:read-detail', 'Xem chi tiết mẫu ca', 'ACTIVE'),
    (NOW(), NOW(), 'shift-template:update', 'Cập nhật mẫu ca', 'ACTIVE'),
    (NOW(), NOW(), 'shift-template:delete', 'Xóa mẫu ca', 'ACTIVE'),

    (NOW(), NOW(), 'inventory-invoice:import', 'Nhập kho', 'ACTIVE'),
    (NOW(), NOW(), 'inventory-invoice:read-by-filter', 'Xem/Lọc hóa đơn nhập kho', 'ACTIVE'),
    (NOW(), NOW(), 'inventory-invoice:read-detail', 'Xem chi tiết hóa đơn nhập', 'ACTIVE'),

    (NOW(), NOW(), 'raw-ingredient:create', 'Tạo nguyên liệu thô', 'ACTIVE'),
    (NOW(), NOW(), 'raw-ingredient:read-by-filter', 'Xem/Lọc danh sách nguyên liệu', 'ACTIVE'),
    (NOW(), NOW(), 'raw-ingredient:read-detail', 'Xem chi tiết nguyên liệu', 'ACTIVE'),
    (NOW(), NOW(), 'raw-ingredient:update', 'Cập nhật nguyên liệu', 'ACTIVE'),

    (NOW(), NOW(), 'recipe:create', 'Tạo công thức pha chế', 'ACTIVE'),
    (NOW(), NOW(), 'recipe:read-by-variant', 'Xem công thức theo biến thể', 'ACTIVE'),
    (NOW(), NOW(), 'recipe:read-by-topping', 'Xem công thức theo topping', 'ACTIVE'),

    (NOW(), NOW(), 'stock-check:start-session', 'Bắt đầu phiên kiểm kho', 'ACTIVE'),
    (NOW(), NOW(), 'stock-check:read-by-filter', 'Xem danh sách phiếu kiểm kho', 'ACTIVE'),
    (NOW(), NOW(), 'stock-check:update-count', 'Cập nhật số lượng kiểm kê', 'ACTIVE'),
    (NOW(), NOW(), 'stock-check:approve-session', 'Duyệt phiếu kiểm kho', 'ACTIVE'),

    (NOW(), NOW(), 'unit-conversion:create', 'Tạo quy đổi đơn vị', 'ACTIVE'),
    (NOW(), NOW(), 'unit-conversion:update', 'Cập nhật quy đổi đơn vị', 'ACTIVE'),

    (NOW(), NOW(), 'order:create', 'Tạo đơn hàng mới', 'ACTIVE'),
    (NOW(), NOW(), 'order:read-history', 'Xem lịch sử đơn hàng', 'ACTIVE'),

    (NOW(), NOW(), 'category:create', 'Tạo danh mục sản phẩm', 'ACTIVE'),
    (NOW(), NOW(), 'category:read', 'Xem danh sách danh mục', 'ACTIVE'),
    (NOW(), NOW(), 'category:read-detail', 'Xem chi tiết danh mục', 'ACTIVE'),
    (NOW(), NOW(), 'category:update', 'Cập nhật danh mục', 'ACTIVE'),
    (NOW(), NOW(), 'category:delete', 'Xóa danh mục', 'ACTIVE'),

    (NOW(), NOW(), 'product:create', 'Tạo sản phẩm mới', 'ACTIVE'),
    (NOW(), NOW(), 'product:read', 'Xem danh sách sản phẩm', 'ACTIVE'),
    (NOW(), NOW(), 'product:read-detail', 'Xem chi tiết sản phẩm', 'ACTIVE'),
    (NOW(), NOW(), 'product:update', 'Cập nhật sản phẩm', 'ACTIVE'),
    (NOW(), NOW(), 'product:update-allow-topping', 'Cập nhật topping cho phép', 'ACTIVE'),
    (NOW(), NOW(), 'product:read-allow-topping', 'Xem topping được phép của sản phẩm', 'ACTIVE'),
    (NOW(), NOW(), 'product:upload-image', 'Upload hình ảnh của sản phẩm', 'ACTIVE'),

    (NOW(), NOW(), 'product-variant:create', 'Tạo biến thể sản phẩm', 'ACTIVE'),
    (NOW(), NOW(), 'product-variant:read', 'Xem danh sách biến thể', 'ACTIVE'),
    (NOW(), NOW(), 'product-variant:read-detail', 'Xem chi tiết biến thể', 'ACTIVE'),
    (NOW(), NOW(), 'product-variant:read-by-product', 'Xem biến thể theo sản phẩm', 'ACTIVE'),
    (NOW(), NOW(), 'product-variant:update', 'Cập nhật biến thể sản phẩm', 'ACTIVE'),

    (NOW(), NOW(), 'product-size:create', 'Tạo kích thước (Size) mới', 'ACTIVE'),
    (NOW(), NOW(), 'product-size:read', 'Xem danh sách kích thước', 'ACTIVE'),
    (NOW(), NOW(), 'product-size:read-active', 'Xem kích thước đang hoạt động', 'ACTIVE'),
    (NOW(), NOW(), 'product-size:update', 'Cập nhật kích thước', 'ACTIVE'),
    (NOW(), NOW(), 'product-size:delete', 'Xóa kích thước', 'ACTIVE'),

    (NOW(), NOW(), 'topping:create', 'Tạo topping mới', 'ACTIVE'),
    (NOW(), NOW(), 'topping:read', 'Xem danh sách topping', 'ACTIVE'),
    (NOW(), NOW(), 'topping:read-detail', 'Xem chi tiết topping', 'ACTIVE'),
    (NOW(), NOW(), 'topping:update', 'Cập nhật topping', 'ACTIVE'),
    (NOW(), NOW(), 'topping:delete', 'Xóa topping', 'ACTIVE'),

    (NOW(), NOW(), 'promotion:create', 'Tạo chương trình khuyến mãi', 'ACTIVE'),
    (NOW(), NOW(), 'promotion:read', 'Xem danh sách khuyến mãi', 'ACTIVE'),
    (NOW(), NOW(), 'promotion:read-detail', 'Xem chi tiết khuyến mãi', 'ACTIVE'),
    (NOW(), NOW(), 'promotion:update', 'Cập nhật khuyến mãi', 'ACTIVE'),
    (NOW(), NOW(), 'promotion:delete', 'Xóa khuyến mãi', 'ACTIVE'),
    (NOW(), NOW(), 'promotion:upload-image', 'Tải lên ảnh khuyến mãi', 'ACTIVE'),

    (NOW(), NOW(), 'membership-rank:read', 'Xem hạng thành viên', 'ACTIVE'),
    (NOW(), NOW(), 'membership-rank:read-detail', 'Xem chi tiết hạng thành viên', 'ACTIVE'),
    (NOW(), NOW(), 'membership-rank:create', 'Tạo hạng thành viên', 'ACTIVE'),
    (NOW(), NOW(), 'membership-rank:update', 'Cập nhật hạng thành viên', 'ACTIVE'),
    (NOW(), NOW(), 'membership-rank:delete', 'Xóa hạng thành viên', 'ACTIVE'),

    (NOW(), NOW(), 'shop:read', 'Xem cửa hàng', 'ACTIVE'),
    (NOW(), NOW(), 'shop:read-detail', 'Xem chi tiết cửa hàng', 'ACTIVE'),
    (NOW(), NOW(), 'shop:create', 'Tạo cửa hàng', 'ACTIVE'),
    (NOW(), NOW(), 'shop:update', 'Cập nhật cửa hàng', 'ACTIVE'),
    (NOW(), NOW(), 'shop:delete', 'Xóa cửa hàng', 'ACTIVE'),

    (NOW(), NOW(), 'dashboard:shop', 'Dashboard hằng ngày của cửa hàng', 'ACTIVE'),
    (NOW(), NOW(), 'dashboard:system', 'Dashboard hằng tháng của system', 'ACTIVE'),

    (NOW(), NOW(), 'notification:read', 'Xem thông báo', 'ACTIVE'),
    (NOW(), NOW(), 'notification:update', 'cập nhật thông báo', 'ACTIVE');