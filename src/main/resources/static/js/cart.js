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
        var cartBookId = $(this).val();
        var price = $("#price_" + cartBookId).data("price");
        var count = $("#count_" + cartBookId).val();
        var cartBookTotal = price * count;
        orderTotalPrice += cartBookTotal;
        
        // 개별 상품의 총 금액 업데이트 (배송비 제외)
        $("#totalPrice_" + cartBookId).html(cartBookTotal.toLocaleString() + "원");
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
    const cartBookId = button.getAttribute('data-id');
    const input = document.querySelector(`#count_${cartBookId}`);
    const stock = parseInt(button.getAttribute('data-stock'));
    const currentValue = parseInt(input.value);
    
    if (currentValue >= stock) {
        Swal.fire({
            icon: 'warning',
            title: '재고 부족',
            text: '재고가 부족합니다.',
            confirmButtonColor: '#4E73DF'
        });
        return;
    }
    
    input.value = currentValue + 1;
    updateQuantity(input);
}

// 수량 감소
function decreaseCount(button) {
    const cartBookId = button.getAttribute('data-id');
    const input = document.querySelector(`#count_${cartBookId}`);
    const currentValue = parseInt(input.value);
    
    if (currentValue > 1) {
        input.value = currentValue - 1;
        updateQuantity(input);
    }
}

// 수량 변경 시 호출되는 함수
function changeCount(input) {
    const cartBookId = input.getAttribute('data-id');
    const stock = parseInt(input.getAttribute('data-stock'));
    let newValue = parseInt(input.value);
    
    if (isNaN(newValue) || newValue < 1) {
        newValue = 1;
    } else if (newValue > stock) {
        Swal.fire({
            icon: 'warning',
            title: '재고 부족',
            text: '재고가 부족합니다.',
            confirmButtonColor: '#4E73DF'
        });
        newValue = stock;
    }
    
    input.value = newValue;
    updateQuantity(input);
}

// 선택된 상품 삭제
function deleteSelectedCartBooks() {
    var selectedCartBooks = [];
    $("input[name=cartChkBox]:checked").each(function() {
        selectedCartBooks.push($(this).val());
    });

    if (selectedCartBooks.length === 0) {
        Swal.fire({
            icon: 'warning',
            title: '선택된 상품 없음',
            text: '삭제할 상품을 선택해주세요.',
            confirmButtonColor: '#4E73DF'
        });
        return;
    }

    Swal.fire({
        title: '상품 삭제',
        text: '장바구니에서 삭제하시겠습니까?',
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#4E73DF',
        cancelButtonColor: '#d33',
        confirmButtonText: '삭제',
        cancelButtonText: '취소'
    }).then((result) => {
        if (result.isConfirmed) {

            $.ajax({
                url: '/cart/books',
                type: 'DELETE',
                data: JSON.stringify(selectedCartBooks),
                contentType: 'application/json',
                success: function() {
                    location.reload();
                },
                error: function(jqXHR, textStatus, errorThrown) {
                    Swal.fire({
                        icon: 'error',
                        title: '오류 발생',
                        text: '상품 삭제 중 오류가 발생했습니다.',
                        confirmButtonColor: '#4E73DF'
                    });
                }
            });
        }
    });
}

// 개별 상품 삭제
function deleteCartBook(btn) {
    var cartBookId = $(btn).data("id");
    
    Swal.fire({
        title: '상품 삭제',
        text: '선택하신 상품을 장바구니에서 삭제하시겠습니까?',
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#4E73DF',
        cancelButtonColor: '#d33',
        confirmButtonText: '삭제',
        cancelButtonText: '취소'
    }).then((result) => {
        if (result.isConfirmed) {

            $.ajax({
                url: '/cart/' + cartBookId,
                type: 'DELETE',
                data: JSON.stringify(cartBookId),
                success: function() {
                    location.reload();
                },
                error: function(jqXHR, textStatus, errorThrown) {
                    Swal.fire({
                        icon: 'error',
                        title: '오류 발생',
                        text: '상품 삭제 중 오류가 발생했습니다.',
                        confirmButtonColor: '#4E73DF'
                    });
                }
            });
        }
    });
}

// 선택된 상품 주문 처리 함수
function orders() {
    var checkedCartBooks = $("input[name=cartChkBox]:checked");

    if(checkedCartBooks.length === 0){
        Swal.fire({
            icon: 'warning',
            title: '선택된 상품 없음',
            text: '주문할 상품을 선택해주세요.',
            confirmButtonColor: '#4E73DF'
        });
        return;
    }

    var cartBooks = [];
    checkedCartBooks.each(function() {
        cartBooks.push({
            cartBookId: $(this).val()
        });
    });

    $.ajax({
        url: "/order/payment",
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify({
            cartBooks: cartBooks
        }),
        success: function(response) {
            location.href = '/order/payment';
        },
        error: function(jqXHR) {
            Swal.fire({
                icon: 'error',
                title: '오류 발생',
                text: jqXHR.responseText,
                confirmButtonColor: '#4E73DF'
            });
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
    const cartBookId = input.getAttribute('data-id');
    const stock = parseInt(input.getAttribute('data-stock'));
    const quantity = parseInt(input.value) || 1;
    
    if (quantity > stock) {
        Swal.fire({
            icon: 'warning',
            title: '재고 부족',
            text: '재고가 부족합니다.',
            confirmButtonColor: '#4E73DF'
        });
        input.value = stock;
        return;
    }
    
    if (quantity < 1) {
        input.value = 1;
        return;
    }
    
    $.ajax({
        url: `/cartBook/${cartBookId}`,
        type: 'PATCH',
        contentType: 'application/json',
        data: JSON.stringify({ 
            count: quantity 
        }),
        success: function(result) {
            // 개별 상품 금액 업데이트
            const price = $("#price_" + cartBookId).data("price");
            const totalPrice = price * quantity;
            $("#totalPrice_" + cartBookId).html(totalPrice.toLocaleString() + "원");
            
            // 전체 금액 재계산
            calculateCheckedPrice();
        },
        error: function(error) {
            console.error('수량 업데이트 실패:', error);
            Swal.fire({
                icon: 'error',
                title: '오류 발생',
                text: '수량 변경에 실패했습니다.',
                confirmButtonColor: '#4E73DF'
            });
        }
    });
}

function updateCartBookPrice(cartBookId) {
    const countInput = document.querySelector(`#count_${cartBookId}`);
    const priceElement = document.querySelector(`#price_${cartBookId}`);
    const totalPriceElement = document.querySelector(`#totalPrice_${cartBookId}`);
    
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
        var cartBookId = $(this).val();
        var price = $("#price_" + cartBookId).data("price");
        var count = $("#count_" + cartBookId).val();
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
