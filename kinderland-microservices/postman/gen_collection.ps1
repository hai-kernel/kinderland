$c = @{
  info = @{
    _postman_id = "kinderland-v2"
    name = "Kinderland - Auth + Product + Order"
    description = "Combined collection. Run Login (Admin) first to auto-save accessToken."
    schema = "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  }
}

function MkReq($method, $url, $auth, $body) {
  $r = @{ method = $method; header = @(); url = @{ raw = $url; host = @("{{baseUrl}}"); path = ($url -replace '\{\{baseUrl\}\}/','' -split '/') } }
  if ($auth) { $r.header += @{ key = "Authorization"; value = "Bearer {{accessToken}}" } }
  if ($body) { $r.header += @{ key = "Content-Type"; value = "application/json" }; $r.body = @{ mode = "raw"; raw = $body } }
  $r
}

function MkItem($name, $method, $url, $auth, $body, $testCode) {
  $item = @{ name = $name; request = (MkReq $method $url $auth $body); response = @() }
  if ($testCode) { $item.event = @(@{ listen = "test"; script = @{ exec = @($testCode); type = "text/javascript" } }) }
  $item
}

$t200 = 'pm.test("Status 200",()=>pm.response.to.have.status(200));'
$t201 = 'pm.test("Status 201",()=>pm.response.to.have.status(201));'
$t400 = 'pm.test("Status 400",()=>pm.response.to.have.status(400));'
$t401 = 'pm.test("Status 401",()=>pm.response.to.have.status(401));'
$t404 = 'pm.test("Status 404",()=>pm.response.to.have.status(404));'
$saveToken = 'const j=pm.response.json();if(j.data&&j.data.accessToken){pm.collectionVariables.set("accessToken",j.data.accessToken);pm.collectionVariables.set("refreshToken",j.data.refreshToken);}' + $t200

# Auth
$auth = @{ name = "Auth"; item = @(
  (MkItem "Login (Admin)" "POST" "{{baseUrl}}/api/v1/auth/login" $false '{"email":"admin@kinderland.vn","password":"Admin@123"}' $saveToken),
  (MkItem "Login (Customer)" "POST" "{{baseUrl}}/api/v1/auth/login" $false '{"email":"customer@kinderland.vn","password":"Customer@123"}' $saveToken),
  (MkItem "Register" "POST" "{{baseUrl}}/api/v1/auth/register" $false '{"username":"testuser01","email":"testuser01@example.com","password":"Test@1234","firstName":"Test","lastName":"User","phone":"0901234567"}' $t201),
  (MkItem "Forgot Password" "POST" "{{baseUrl}}/api/v1/auth/forgot-password" $false '{"email":"customer@kinderland.vn"}' $t200),
  (MkItem "Reset Password with OTP" "POST" "{{baseUrl}}/api/v1/auth/reset-password" $false '{"email":"customer@kinderland.vn","otp":"123456","newPassword":"NewPass@123"}' $t200),
  (MkItem "Logout" "POST" "{{baseUrl}}/api/v1/auth/logout" $true $null $t200)
)}

# Account
$account = @{ name = "Account"; item = @(
  (MkItem "Get My Profile" "GET" "{{baseUrl}}/api/v1/account/me" $true $null $t200),
  (MkItem "Get Profile No Token (401)" "GET" "{{baseUrl}}/api/v1/account/me" $false $null $t401),
  (MkItem "Update Profile" "POST" "{{baseUrl}}/api/v1/account/update-profile" $true '{"firstName":"Updated","lastName":"Name","phone":"0987654321"}' $t200)
)}

# Address
$address = @{ name = "Address"; item = @(
  (MkItem "Get My Addresses" "GET" "{{baseUrl}}/api/v1/address/my-addresses" $true $null $t200),
  (MkItem "Create Address" "POST" "{{baseUrl}}/api/v1/address/create" $true '{"street":"123 Le Loi","provinceId":202,"provinceName":"TP. Ho Chi Minh","districtId":1452,"districtName":"Quan 1","wardId":20211,"wardName":"Phuong Ben Nghe"}' ('const j=pm.response.json();if(j.data&&j.data.addressId){pm.collectionVariables.set("addressId",String(j.data.addressId));}' + $t201)),
  (MkItem "Update Address" "PUT" "{{baseUrl}}/api/v1/address/update/{{addressId}}" $true '{"street":"456 Nguyen Hue","provinceId":202,"provinceName":"TP. Ho Chi Minh","districtId":1452,"districtName":"Quan 1","wardId":20211,"wardName":"Phuong Ben Nghe"}' $t200),
  (MkItem "Delete Address" "DELETE" "{{baseUrl}}/api/v1/address/delete/{{addressId}}" $true $null $t200)
)}

# Admin
$admin = @{ name = "Admin - Accounts"; item = @(
  (MkItem "Get All Accounts" "GET" "{{baseUrl}}/api/v1/admin/accounts" $true $null $t200),
  (MkItem "Create Account (Admin)" "POST" "{{baseUrl}}/api/v1/admin/accounts/create" $true '{"username":"manager03","email":"manager03@example.com","phone":"0999999997","password":"Manager@123","firstName":"Store","lastName":"Manager","role":"MANAGER"}' ('const j=pm.response.json();if(j.data&&j.data.id){pm.collectionVariables.set("accountId",String(j.data.id));}' + $t201)),
  (MkItem "Delete Account (Admin)" "DELETE" "{{baseUrl}}/api/v1/admin/accounts/delete/{{accountId}}" $true $null $t200)
)}

# Category
$category = @{ name = "Category (Product Service)"; item = @(
  (MkItem "Get All Categories" "GET" "{{baseUrl}}/api/v1/categories" $false $null $t200),
  (MkItem "Get Category By ID" "GET" "{{baseUrl}}/api/v1/categories/{{categoryId}}" $false $null $t200),
  (MkItem "Create Category (Admin)" "POST" "{{baseUrl}}/api/v1/categories" $true '{"name":"Do choi tre em","parentId":null}' ('const j=pm.response.json();if(j.data&&j.data.id){pm.collectionVariables.set("categoryId",String(j.data.id));}' + $t201)),
  (MkItem "Update Category (Admin)" "PUT" "{{baseUrl}}/api/v1/categories/{{categoryId}}" $true '{"name":"Do choi tre em (Updated)","parentId":null}' $t200),
  (MkItem "Delete Category (Admin)" "DELETE" "{{baseUrl}}/api/v1/categories/{{categoryId}}" $true $null $t200)
)}

# Product
$product = @{ name = "Product (Product Service)"; item = @(
  (MkItem "Get All Products" "GET" "{{baseUrl}}/api/v1/products" $false $null $t200),
  (MkItem "Get Product By ID" "GET" "{{baseUrl}}/api/v1/products/{{productId}}" $false $null $t200),
  (MkItem "Get Product Not Found (404)" "GET" "{{baseUrl}}/api/v1/products/99999" $false $null $t404),
  (MkItem "Create Product (Admin)" "POST" "{{baseUrl}}/api/v1/products" $true '{"name":"Bo xep hinh 100 manh","description":"Do choi tri tue 3-8 tuoi","ageRange":"3-8","gender":"UNISEX","price":150000,"stockQuantity":50,"categoryId":1}' ('const j=pm.response.json();if(j.data&&j.data.id){pm.collectionVariables.set("productId",String(j.data.id));}' + $t201)),
  (MkItem "Create Product Missing Name (400)" "POST" "{{baseUrl}}/api/v1/products" $true '{"name":"","price":150000,"stockQuantity":50}' $t400),
  (MkItem "Create Product No Token (401)" "POST" "{{baseUrl}}/api/v1/products" $false '{"name":"Test","price":100000,"stockQuantity":10}' $t401),
  (MkItem "Update Product (Admin)" "PUT" "{{baseUrl}}/api/v1/products/{{productId}}" $true '{"name":"Bo xep hinh (Updated)","description":"Cap nhat","ageRange":"4-10","gender":"UNISEX","price":175000,"stockQuantity":40,"categoryId":1}' $t200),
  (MkItem "Delete Product (Admin)" "DELETE" "{{baseUrl}}/api/v1/products/{{productId}}" $true $null $t200)
)}

# Cart
$cart = @{ name = "Cart (Order Service)"; item = @(
  (MkItem "Get My Cart" "GET" "{{baseUrl}}/api/v1/cart" $true $null $t200),
  (MkItem "Get Cart No Token (401)" "GET" "{{baseUrl}}/api/v1/cart" $false $null $t401),
  (MkItem "Add Item to Cart" "POST" "{{baseUrl}}/api/v1/cart/items" $true ('{"productId":' + '{{productId}}' + ',"quantity":2}') $t200),
  (MkItem "Add Item Invalid Qty (400)" "POST" "{{baseUrl}}/api/v1/cart/items" $true ('{"productId":' + '{{productId}}' + ',"quantity":0}') $t400),
  (MkItem "Clear Cart" "DELETE" "{{baseUrl}}/api/v1/cart" $true $null $t200)
)}

# Order
$order = @{ name = "Order (Order Service)"; item = @(
  (MkItem "Create Order" "POST" "{{baseUrl}}/api/v1/orders" $true ('{"items":[{"productId":' + '{{productId}}' + ',"quantity":1}]}') ('const j=pm.response.json();if(j.data&&j.data.id){pm.collectionVariables.set("orderId",String(j.data.id));}' + $t201)),
  (MkItem "Create Order Empty Items (400)" "POST" "{{baseUrl}}/api/v1/orders" $true '{"items":[]}' $t400),
  (MkItem "Create Order No Token (401)" "POST" "{{baseUrl}}/api/v1/orders" $false '{"items":[{"productId":1,"quantity":1}]}' $t401),
  (MkItem "Get My Orders" "GET" "{{baseUrl}}/api/v1/orders" $true $null $t200),
  (MkItem "Get Order By ID" "GET" "{{baseUrl}}/api/v1/orders/{{orderId}}" $true $null $t200),
  (MkItem "Get Order Not Mine (403/404)" "GET" "{{baseUrl}}/api/v1/orders/99999" $true $null 'pm.test("Forbidden or Not Found",()=>{pm.expect([403,404]).to.include(pm.response.code);});')
)}

$c.item = @($auth, $account, $address, $admin, $category, $product, $cart, $order)
$c.variable = @(
  @{key="baseUrl";value="http://localhost:8080";type="string"},
  @{key="accessToken";value="";type="string"},
  @{key="refreshToken";value="";type="string"},
  @{key="addressId";value="1";type="string"},
  @{key="accountId";value="1";type="string"},
  @{key="categoryId";value="1";type="string"},
  @{key="productId";value="1";type="string"},
  @{key="orderId";value="1";type="string"}
)

$c | ConvertTo-Json -Depth 20 -Compress | Set-Content "C:\Users\Admin\Downloads\kinderland\kinderland-microservices\postman\Kinderland_Auth_Product_Order.postman_collection.json" -Encoding UTF8
Write-Host "Done - valid JSON written!"
