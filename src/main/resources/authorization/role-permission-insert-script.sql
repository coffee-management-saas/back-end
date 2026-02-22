TRUNCATE TABLE public.role_permission CASCADE;

-- SYSTEM_ADMIN
INSERT INTO public.role_permission (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM public.role r
         CROSS JOIN public.permission p
WHERE r.name = 'SYSTEM';

-- SHOP_MANAGER
INSERT INTO public.role_permission (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM public.role r
         JOIN public.permission p ON p.permission_name IN (
                                                           'promotion:create', 'promotion:read', 'promotion:read-detail',
                                                           'promotion:update', 'promotion:delete', 'promotion:upload-image',
                                                           'topping:create', 'topping:read', 'topping:read-detail',
                                                           'topping:update', 'topping:delete',
                                                           'product-size:create', 'product-size:read', 'product-size:read-active',
                                                           'product-size:update', 'product-size:delete',
                                                           'employee:create', 'employee:read', 'employee:read-detail',
                                                           'employee:update', 'employee:delete',
                                                           'employee-unavailability:create', 'employee-unavailability:read',
                                                           'employee-unavailability:read-detail', 'employee-unavailability:update', 'employee-unavailability:delete',
                                                           'schedule:create', 'schedule:read', 'schedule:read-detail',
                                                           'schedule:read-by-employee', 'schedule:update', 'schedule:delete',
                                                           'shift-template:create', 'shift-template:read', 'shift-template:read-detail',
                                                           'shift-template:update', 'shift-template:delete',
                                                           'inventory-invoice:import', 'inventory-invoice:read-by-filter', 'inventory-invoice:read-detail',
                                                           'raw-ingredient:create', 'raw-ingredient:read-by-filter',
                                                           'raw-ingredient:read-detail', 'raw-ingredient:update',
                                                           'recipe:create', 'recipe:read-by-variant', 'recipe:read-by-topping',
                                                           'stock-check:start-session', 'stock-check:read-by-filter',
                                                           'stock-check:update-count', 'stock-check:approve-session',
                                                           'unit-conversion:create', 'unit-conversion:update',
                                                           'order:create',
                                                           'category:create', 'category:read', 'category:read-detail',
                                                           'category:update', 'category:delete',
                                                           'product:create', 'product:read', 'product:read-detail', 'product:update',
                                                           'product:update-allow-topping', 'product:read-allow-topping',
                                                           'product-variant:create', 'product-variant:read',
                                                           'product-variant:read-detail', 'product-variant:read-by-product'
    )
WHERE r.name = 'SHOP';

-- SHOP_STAFF
INSERT INTO public.role_permission (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM public.role r
         JOIN public.permission p ON p.permission_name IN (
                                                           'employee-unavailability:read', 'employee-unavailability:read-detail',
                                                           'employee-unavailability:create', 'employee-unavailability:update', 'employee-unavailability:delete',
                                                           'schedule:read', 'schedule:read-detail', 'schedule:read-by-employee',
                                                           'shift-template:read', 'shift-template:read-detail',
                                                           'inventory-invoice:import', 'inventory-invoice:read-by-filter', 'inventory-invoice:read-detail',
                                                           'raw-ingredient:read-by-filter', 'raw-ingredient:read-detail',
                                                           'recipe:read-by-variant', 'recipe:read-by-topping',
                                                           'order:create',
                                                           'category:read', 'category:read-detail',
                                                           'product:read', 'product:read-detail', 'product:read-allow-topping',
                                                           'product-variant:read', 'product-variant:read-detail', 'product-variant:read-by-product',
                                                           'product-size:read', 'product-size:read-active',
                                                           'topping:read', 'topping:read-detail',
                                                           'promotion:read', 'promotion:read-detail',
                                                           'stock-check:update-count', 'stock-check:read-by-filter'
    )
WHERE r.name = 'EMPLOYEE';

-- CUSTOMER
INSERT INTO public.role_permission (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM public.role r
         JOIN public.permission p ON p.permission_name IN (
                                                           'order:create',
                                                           'product:read', 'product:read-detail', 'product:read-allow-topping',
                                                           'product-variant:read', 'product-variant:read-detail', 'product-variant:read-by-product',
                                                           'product-size:read', 'product-size:read-active',
                                                           'topping:read', 'topping:read-detail',
                                                           'promotion:read', 'promotion:read-detail',
                                                           'category:read'
    )
WHERE r.name = 'CUSTOMER';