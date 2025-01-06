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
    
    const paymentData = {
        pg: "kakaopay.TC0ONETIME",
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
            amount: orderDto.totalPrice,
            status: rsp.status
        }),
        beforeSend: function(xhr) {
            xhr.setRequestHeader(header, token);
        },
        success: function(response) {
            if (response.success && response.orderId) {
                // 주문 ID가 있으면 바로 성공 페이지로 이동
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
        
        // 최종 금액 표시 (순수 상품 금액 + 배송비)
        const finalTotal = originalPrice + shippingFee;
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
    const usedPointSpan = document.getElementById('usedPoint');
    const finalTotalPriceSpan = document.getElementById('finalTotalPrice');
    const productPriceText = document.getElementById('productPrice').textContent;
    const deliveryFeeText = document.getElementById('deliveryFee').textContent;
    
    // 입력된 포인트
    let points = parseInt(pointInput.value) || 0;
    const maxPoints = parseInt(pointInput.getAttribute('max')) || 0;
    const productPrice = parseInt(productPriceText.replace(/[^0-9]/g, ''));
    const deliveryFee = parseInt(deliveryFeeText.replace(/[^0-9]/g, ''));

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
                
                // UI 업데이트
                usedPointSpan.textContent = points.toLocaleString();
                
                // 최종 금액 계산 및 표시
                const finalTotal = productPrice + deliveryFee - points;
                finalTotalPriceSpan.textContent = finalTotal.toLocaleString() + '원';
                
                // 결제 금액 업데이트
                orderDto.totalPrice = finalTotal;
                
                alert(response.message);
            } else {
                alert(response.message);
                pointInput.value = 0;
                usedPointSpan.textContent = '0';
                
                // 최종 금액 원상복구
                const finalTotal = productPrice + deliveryFee;
                finalTotalPriceSpan.textContent = finalTotal.toLocaleString() + '원';
            }
        },
        error: function(xhr) {
            const response = xhr.responseJSON;
            alert(response?.message || '포인트 적용 중 오류가 발생했습니다.');
            pointInput.value = 0;
            usedPointSpan.textContent = '0';
            
            // 최종 금액 원상복구
            const finalTotal = productPrice + deliveryFee;
            finalTotalPriceSpan.textContent = finalTotal.toLocaleString() + '원';
        }
    });
} 