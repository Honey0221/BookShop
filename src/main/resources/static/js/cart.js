$(document).ready(function() {
    // 페이지 로드 시 초기 총 금액 계산
    calculateCheckedPrice();
    
    // 체크박스 변경 이벤트
    $("input[name=cartChkBox]").change(function() {
        calculateCheckedPrice();
    });

    // 배송비 안내 버튼 클릭 이벤트
    $(".btn_shipping_info").click(function() {
        $("#shippingInfoModal").show();
    });

    // 모달 외부 클릭 시 닫기
    $(window).click(function(e) {
        if (e.target == $("#shippingInfoModal")[0]) {
            $("#shippingInfoModal").hide();
        }
    });
});

// 체크된 상품만의 금액 계산 함수
function calculateCheckedPrice() {
    var orderTotalPrice = 0;
    var deliveryFee = 0;

    // 체크된 상품만의 주문 금액 계산 (순수 상품 금액)
    $("input[name=cartChkBox]:checked").each(function () {
        var cartItemId = $(this).val();
        var price = $("#price_" + cartItemId).data("price");
        var count = $("#count_" + cartItemId).val();
        var itemTotal = price * count;
        orderTotalPrice += itemTotal;
        
        // 개별 상품의 총 금액 업데이트 (배송비 제외)
        $("#totalPrice_" + cartItemId).html(itemTotal.toLocaleString() + "원");
    });

    // 배송비 계산 (15,000원 미만 주문 시 3,000원)
    if(orderTotalPrice > 0 && orderTotalPrice < 15000) {
        deliveryFee = 3000;
    } else {
        deliveryFee = 0;
    }

    // 화면 업데이트
    $("#totalPrice").html(orderTotalPrice.toLocaleString());  // 순수 상품 금액
    $("#deliveryFee").html(deliveryFee.toLocaleString());    // 배송비
    $("#orderTotalPrice").html((orderTotalPrice + deliveryFee).toLocaleString());  // 최종 결제 금액
}

// 전체 선택/해제 토글 함수
function checkAll() {
    if ($("#checkall").prop("checked")) {
        $("input[name=cartChkBox]").prop("checked", true);
    } else {
        $("input[name=cartChkBox]").prop("checked", false);
    }
    calculateCheckedPrice();
}

// 개별 체크박스 해제 시 전체 선택 체크박스 상태 업데이트
function uncheck() {
    if (!$("input[name=cartChkBox]").prop("checked")) {
        $("#checkall").prop("checked", false);
    }
    calculateCheckedPrice();
}

// 수량 증가
function increaseCount(button) {
    const input = button.previousElementSibling;
    const itemId = input.id.split('_')[1];
    const newCount = parseInt(input.value) + 1;
    input.value = newCount;
    updateQuantity(itemId, newCount);
}

// 수량 감소
function decreaseCount(button) {
    const input = button.nextElementSibling;
    const itemId = input.id.split('_')[1];
    const newCount = Math.max(1, parseInt(input.value) - 1);
    input.value = newCount;
    updateQuantity(itemId, newCount);
}

// 수량 변경 시 호출되는 함수
function changeCount(input) {
    const itemId = input.id.split('_')[1];
    const newCount = Math.max(1, parseInt(input.value));
    input.value = newCount;
    updateQuantity(itemId, newCount);
}

// 선택된 상품 삭제
function deleteSelectedItems() {
    var selectedItems = [];
    $("input[name=cartChkBox]:checked").each(function() {
        selectedItems.push($(this).val());
    });

    if (selectedItems.length === 0) {
        alert("삭제할 상품을 선택해주세요.");
        return;
    }

    if (!confirm("장바구니에서 삭제하시겠습니까?")) {
        return;
    }

    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");

    $.ajax({
        url: '/cart/items',
        type: 'DELETE',
        data: JSON.stringify(selectedItems),
        contentType: 'application/json',
        beforeSend: function(xhr) {
            xhr.setRequestHeader(header, token);
        },
        success: function() {
            location.reload();
        },
        error: function(jqXHR, textStatus, errorThrown) {
            alert("상품 삭제 중 오류가 발생했습니다.");
        }
    });
}

// 개별 상품 삭제
function deleteCartItem(btn) {
    var cartItemId = $(btn).data("id");
    
    if (!confirm("선택하신 상품을 장바구니에서 삭제하시겠습니까?")) {
        return;
    }

    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");

    $.ajax({
        url: '/cart/' + cartItemId,
        type: 'DELETE',
        data: JSON.stringify(cartItemId),
        beforeSend: function(xhr) {
            xhr.setRequestHeader(header, token);
        },
        success: function() {
            location.reload();
        },
        error: function(jqXHR, textStatus, errorThrown) {
            alert("상품 삭제 중 오류가 발생했습니다.");
        }
    });
}

// 선택된 상품 주문 처리 함수
function orders() {
    // 선택된 상품들의 cartItemId 수집
    var checkedItems = $("input[name=cartChkBox]:checked");

    // 선택된 상품이 없는 경우
    if(checkedItems.length === 0){
        alert("주문할 상품을 선택해주세요.");
        return;
    }

    // 선택된 상품들의 정보를 배열에 추가
    var cartItems = [];
    checkedItems.each(function() {
        cartItems.push({
            cartItemId: $(this).val()
        });
    });

    // 주문 데이터를 OrderController로 전송
    $.ajax({
        url: "/order/payment",
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify({
            cartItems: cartItems
        }),
        success: function(response) {
            location.href = '/order/payment';
        },
        error: function(jqXHR) {
            alert(jqXHR.responseText);
        }
    });
}

// 모달 닫기 함수
function closeModal() {
    $("#shippingInfoModal").hide();
}

// 장바구니 목록 토글 함수
function toggleCartList(button) {
    const container = document.querySelector('.cart_items_container');
    const icon = button.querySelector('i');
    
    if (container.classList.contains('expanded')) {
        // 접기
        container.classList.remove('expanded');
        container.classList.add('collapsed');
        button.classList.add('collapsed');
    } else {
        // 펼치기
        container.classList.remove('collapsed');
        container.classList.add('expanded');
        button.classList.remove('collapsed');
    }
}

// 페이지 로드 시 기본적으로 펼쳐진 상태로 시작
document.addEventListener('DOMContentLoaded', function() {
    const container = document.querySelector('.cart_items_container');
    container.classList.add('expanded');
});

function updateQuantity(input) {
    const itemId = input.getAttribute('data-id');
    const quantity = parseInt(input.value) || 1;
    
    if (quantity < 1) {
        input.value = 1;
        return;
    }

    const token = document.querySelector("meta[name='_csrf']").content;
    const header = document.querySelector("meta[name='_csrf_header']").content;
    
    $.ajax({
        url: `/cartItem/${itemId}`,
        type: 'PATCH',
        contentType: 'application/json',
        data: JSON.stringify({ 
            count: quantity 
        }),
        beforeSend: function(xhr) {
            xhr.setRequestHeader(header, token);
        },
        success: function(result) {
            updateItemPrice(itemId);
            calculateCheckedPrice();
        },
        error: function(error) {
            console.error('수량 업데이트 실패:', error);
            alert('수량 변경에 실패했습니다.');
            location.reload();
        }
    });
}

function updateItemPrice(itemId) {
    const countInput = document.querySelector(`#count_${itemId}`);
    const priceElement = document.querySelector(`#price_${itemId}`);
    const totalPriceElement = document.querySelector(`#totalPrice_${itemId}`);
    
    const quantity = parseInt(countInput.value);
    const price = parseInt(priceElement.getAttribute('data-price'));
    const totalPrice = quantity * price;  // 순수 상품 금액만 계산
    
    totalPriceElement.textContent = totalPrice.toLocaleString() + '원';
}

function updateTotalPrice() {
    var orderTotalPrice = 0;
    var deliveryFee = 0;

    // 체크된 상품만의 주문 금액 계산
    $("input[name=cartChkBox]:checked").each(function () {
        var cartItemId = $(this).val();
        var price = $("#price_" + cartItemId).data("price");
        var count = $("#count_" + cartItemId).val();
        orderTotalPrice += price * count;
    });

    // 배송비 계산 (15,000원 미만 주문 시 3,000원)
    if(orderTotalPrice > 0 && orderTotalPrice < 15000) {
        deliveryFee = 3000;
    }

    // 화면 업데이트
    $("#totalPrice").html(orderTotalPrice.toLocaleString());
    $("#deliveryFee").html(deliveryFee.toLocaleString());
    $("#orderTotalPrice").html((orderTotalPrice + deliveryFee).toLocaleString());
}

function decreaseCount(button) {
    const itemId = button.getAttribute('data-id');
    const input = document.querySelector(`#count_${itemId}`);
    const currentValue = parseInt(input.value);
    
    if (currentValue > 1) {
        input.value = currentValue - 1;
        updateQuantity(input);
    }
}

function increaseCount(button) {
    const itemId = button.getAttribute('data-id');
    const input = document.querySelector(`#count_${itemId}`);
    input.value = parseInt(input.value) + 1;
    updateQuantity(input);
}

function changeCount(input) {
    updateQuantity(input);
}

function updateItemPrice(itemId) {
    const countInput = document.querySelector(`#count_${itemId}`);
    const priceElement = document.querySelector(`#price_${itemId}`);
    const totalPriceElement = document.querySelector(`#totalPrice_${itemId}`);
    
    const quantity = parseInt(countInput.value);
    const price = parseInt(priceElement.getAttribute('data-price'));
    const totalPrice = quantity * price;
    
    // 개별 상품의 금액은 배송비를 제외한 순수 상품 금액만 표시
    totalPriceElement.textContent = totalPrice.toLocaleString() + '원';
} 