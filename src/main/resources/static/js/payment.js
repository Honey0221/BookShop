// IMP 초기화
(function() {
    var IMP = window.IMP;
    IMP.init("imp80047713");
})();

// 카카오페이 결제 처리
function requestPay() {
    const token = $("meta[name='_csrf']").attr("content");
    const header = $("meta[name='_csrf_header']").attr("content");
    const merchantUid = `ORDER-${new Date().getTime()}`;
    
    // 최종 결제 금액 계산
    let finalAmount = orderDto.totalPrice;
    
    const paymentData = {
        pg: "kakaopay.TC0ONETIME",
        pay_method: "card",
        merchant_uid: merchantUid,
        name: orderDto.orderName,
        amount: finalAmount, // 최종 결제 금액 사용
        buyer_email: orderDto.email,
        buyer_name: orderDto.name,
        buyer_tel: orderDto.phone,
        buyer_addr: orderDto.address
    };

    window.IMP.request_pay(paymentData, function(rsp) {
        if (rsp.success) {
            verifyPayment(rsp, token, header, merchantUid);
        } else {
            alert("결제에 실패했습니다. " + rsp.error_msg);
        }
    });
}

// 일반 카드 결제 처리
function requestCardPayment() {
    const token = $("meta[name='_csrf']").attr("content");
    const header = $("meta[name='_csrf_header']").attr("content");
    const merchantUid = `ORDER-${new Date().getTime()}`;
    
    const paymentData = {
        pg: "nice_v2.iamport03m",
        pay_method: "card",
        merchant_uid: merchantUid,
        name: orderDto.orderName,
        amount: orderDto.totalPrice,
        buyer_email: orderDto.email,
        buyer_name: orderDto.name,
        buyer_tel: orderDto.phone,
        buyer_addr: orderDto.address
    };

    window.IMP.request_pay(paymentData, function(rsp) {
        if (rsp.imp_uid && rsp.merchant_uid && !rsp.error_code) {
            verifyPayment(rsp, token, header, merchantUid);
        } else {
            handlePaymentError(rsp);
        }
    });
}

// 결제 검증 및 주문 생성
function verifyPayment(rsp, token, header, merchantUid) {
    $.ajax({
        url: "/orders/verify",
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify({
            impUid: rsp.imp_uid,
            merchantUid: merchantUid,
            amount: orderDto.totalPrice, // 최종 결제 금액 사용
            status: rsp.status
        }),
        beforeSend: function(xhr) {
            xhr.setRequestHeader(header, token);
        },
        success: function(response) {
            if (response.success && response.orderId) {
                location.href = "/order/success/" + response.orderId;
            } else {
                alert("결제 검증에 실패했습니다: " + response.message);
            }
        },
        error: function(xhr) {
            console.error("검증 오류:", xhr.responseText);
            alert("결제 검증 중 오류가 발생했습니다.");
        }
    });
}

// 결제 에러 처리
function handlePaymentError(rsp) {
    if (rsp.error_code === "USER_CANCEL") {
        alert("결제가 취소되었습니다.");
    } else {
        const errorMessage = rsp.error_msg || "결제 처리 중 오류가 발생했습니다.";
        alert(errorMessage);
    }
    console.error("결제 실패 또는 취소:", rsp);
}

// 결제 방식 선택
function requestPayment() {
    const selectedPaymentMethod = document.querySelector('input[name="paymentMethod"]:checked');
    
    if (!selectedPaymentMethod) {
        alert('결제 수단을 선택해주세요.');
        return;
    }
    
    const paymentMethod = selectedPaymentMethod.value;
    
    if (paymentMethod === 'kakaopay') {
        requestPay();
    } else if (paymentMethod === 'card') {
        requestCardPayment();
    }
}

// 결제 수단 선택 UI 처리
function selectPaymentMethod(method) {
    document.querySelectorAll('.payment-method-option').forEach(option => {
        option.classList.remove('selected');
    });
    
    const selectedOption = document.querySelector(`#${method}`).closest('.payment-method-option');
    selectedOption.classList.add('selected');
    
    document.querySelector(`#${method}`).checked = true;
}

document.addEventListener('DOMContentLoaded', function() {
    console.log('OrderDto data:', orderDto);
    calculateTotalWithShipping();
});

function calculateTotalWithShipping() {
    // 순수 상품 금액 계산
    const originalPrice = Number(orderDto.totalPrice) || 0;  // 이미 순수 상품 금액
    const quantity = Number(orderDto.count) || 1;
    
    // 개별 상품 가격 계산
    const unitPrice = Math.floor(originalPrice / quantity);
    
    // 배송비 계산
    const shippingFee = originalPrice < 15000 ? 3000 : 0;
    
    // 화면 업데이트
    if (!isNaN(originalPrice)) {
        // 개별 상품 가격 표시
        document.querySelectorAll('.item-price').forEach(el => {
            el.textContent = unitPrice.toLocaleString() + '원';
        });
        
        // 상품 총액 표시 (순수 상품 금액)
        document.getElementById('productPrice').textContent = 
            originalPrice.toLocaleString() + '원';
        
        // 배송비 표시
        document.getElementById('deliveryFee').textContent = 
            shippingFee.toLocaleString() + '원';
        
        // 사용된 포인트와 쿠폰 할인액 가져오기
        const usedPoints = parseInt(document.getElementById('usePoint').textContent.replace(/[^0-9]/g, '')) || 0;
        const usedCoupon = parseInt(document.getElementById('couponSelect').value) || 0;
        
        // 최종 금액 표시 (순수 상품 금액 + 배송비 - 포인트 - 쿠폰)
        const finalTotal = originalPrice + shippingFee - usedPoints - usedCoupon;
        document.getElementById('finalTotalPrice').textContent = 
            finalTotal.toLocaleString() + '원';
            
        // orderDto 업데이트
        orderDto.totalPrice = finalTotal;
    }
}

function showShippingInfo() {
    document.getElementById('shippingInfoModal').style.display = 'block';
}

function closeModal() {
    document.getElementById('shippingInfoModal').style.display = 'none';
}

// 모달 외부 클릭시 닫기
window.onclick = function(event) {
    const modal = document.getElementById('shippingInfoModal');
    if (event.target == modal) {
        modal.style.display = 'none';
    }
}

// Point system functions
function applyPoint() {
    const pointInput = document.getElementById('usePoint');
    const pointDiscount = document.getElementById('pointDiscount');
    // const productPrice = parseInt($("#productPrice").text().replace(/[^0-9]/g, '')) || 0;
    // const deliveryFee = parseInt($("#deliveryFee").text().replace(/[^0-9]/g, '')) || 0;
    
    // 입력된 포인트
    let points = parseInt(pointInput.value) || 0;
    // const maxPoints = parseInt(pointInput.getAttribute('max')) || 0;

    // CSRF 토큰 가져오기
    const token = $("meta[name='_csrf']").attr("content");
    const header = $("meta[name='_csrf_header']").attr("content");
    
    // 서버에 포인트 적용 요청
    $.ajax({
        url: '/order/apply-points',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({ points: points }),
        beforeSend: function(xhr) {
            xhr.setRequestHeader(header, token);
        },
        success: function(response) {
            if (response.success) {
                // 포인트 적용 성공
                points = response.appliedPoints;
                pointInput.value = points;
                pointDiscount.textContent = points.toLocaleString() + '원';
                updateTotalPrice();
                alert(response.message);
            } else {
                alert(response.message);
                pointInput.value = 0;
                pointDiscount.textContent = '0원';
                updateTotalPrice();
            }
        },
        error: function(xhr) {
            const response = xhr.responseJSON;
            alert(response?.message || '포인트 적용 중 오류가 발생했습니다.');
            pointInput.value = 0;
            pointDiscount.textContent = '0원';
            updateTotalPrice();
        }
    });
}

// Coupon system functions
function applyCoupon() {
    const productPrice = parseInt($("#productPrice").text().replace(/[^0-9]/g, "")) || 0;
    const selectedCoupon = $("#couponSelect").val();
    
    if (!selectedCoupon) {
        alert("쿠폰을 선택해주세요.");
        return;
    }
    
    if (productPrice < 15000) {
        alert("15,000원 이상 구매 시에만 쿠폰을 사용할 수 있습니다.");
        return;
    }

    const token = $("meta[name='_csrf']").attr("content");
    const header = $("meta[name='_csrf_header']").attr("content");

    $.ajax({
        url: '/order/apply-coupon',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            orderAmount: productPrice,
            couponAmount: parseInt(selectedCoupon)
        }),
        beforeSend: function(xhr) {
            xhr.setRequestHeader(header, token);
        },
        success: function(response) {
            if (response.success) {
                const discountAmount = response.discountAmount;
                $("#couponDiscount").text(discountAmount.toLocaleString() + "원");
                updateTotalPrice();
                alert("쿠폰이 적용되었습니다.");
                $("#couponSelect").prop("disabled", true);
                $("#applyCouponBtn").prop("disabled", true);
            } else {
                alert(response.message || "쿠폰 적용에 실패했습니다.");
            }
        },
        error: function(xhr) {
            const errorMessage = xhr.responseJSON ? xhr.responseJSON.message : "쿠폰 적용에 실패했습니다.";
            alert(errorMessage);
            $("#couponSelect").val("");
        }
    });
}

function updateTotalPrice() {
    const productPrice = parseInt($("#productPrice").text().replace(/[^0-9]/g, "")) || 0;
    const pointDiscount = parseInt($("#pointDiscount").text().replace(/[^0-9]/g, "")) || 0;
    const couponDiscount = parseInt($("#couponDiscount").text().replace(/[^0-9]/g, "")) || 0;
    const deliveryFee = productPrice < 15000 ? 3000 : 0;
    
    const totalPrice = productPrice + deliveryFee - pointDiscount - couponDiscount;
    $("#finalTotalPrice").text(totalPrice.toLocaleString() + "원");
    $("#deliveryFee").text(deliveryFee.toLocaleString() + "원");
    
    // orderDto 업데이트
    if (orderDto) {
        orderDto.totalPrice = totalPrice;
    }
} 