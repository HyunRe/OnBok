function handleMinus(cid) {
    const cartRow = document.querySelector(`[data-cid="${cid}"]`);
    const quantity = parseInt(cartRow.querySelector('.cart-quantity').innerText);
    if (quantity > 1) {
        updateCart(cid, quantity - 1);
    } else {
        alert("최소 수량은 1개입니다.");
    }
}

function handlePlus(cid) {
    const cartRow = document.querySelector(`[data-cid="${cid}"]`);
    const quantity = parseInt(cartRow.querySelector('.cart-quantity').innerText);
    updateCart(cid, quantity + 1);
}

function handleTrash(cid) {
    if (confirm("정말 삭제하시겠습니까?")) {
        updateCart(cid, 0); // 수량을 0으로 설정하여 삭제
    }
}

function updateCart(cid, quantity) {
    fetch('/api/carts/update', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ id: cid, quantity: quantity }),
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('장바구니 업데이트에 실패했습니다.');
        }
        return response.json();
    })
    .then(result => {
        const data = result.data; // OnBokResponse 구조에 맞게 data 추출

        // UI 업데이트
        document.querySelector('#totalPrice').value = data.totalPrice.toLocaleString();
        document.querySelector('#deliveryCost').value = data.deliveryCost.toLocaleString();
        document.querySelector('#totalPriceIncludingDeliveryCost').value = data.totalPriceIncludingDeliveryCost.toLocaleString();

        const cartRow = document.querySelector(`[data-cid="${cid}"]`);
        if (quantity === 0) {
            cartRow.remove(); // 아이템 삭제
            // 장바구니가 비었으면 페이지 새로고침
            if (document.querySelectorAll('[data-cid]').length === 0) {
                location.reload();
            }
        } else {
            cartRow.querySelector('.cart-quantity').innerText = quantity;
            cartRow.querySelector('.cart-subtotal').innerText = data.subTotal.toLocaleString();
        }
    })
    .catch(error => {
        console.error("Error:", error);
        alert("카트 업데이트 중 오류가 발생했습니다: " + error.message);
    });
}

function openPostcodePopup() {
    new daum.Postcode({
        oncomplete: function(data) {
            const address = data.roadAddress ? data.roadAddress : data.jibunAddress;
            document.getElementById('postcode').value = data.zonecode;
            document.getElementById('address').value = address;
            document.getElementById('detail-address').focus();
        }
    }).open();
}

// 저장된 배송지 불러오기
function loadSavedAddress() {
    const select = document.getElementById('saved-address');
    const selectedOption = select.options[select.selectedIndex];

    if (select.value === '') {
        // 직접 입력 선택 시 필드 초기화
        document.getElementById('recipient-name').value = '';
        document.getElementById('tel').value = '';
        document.getElementById('postcode').value = '';
        document.getElementById('address').value = '';
        document.getElementById('detail-address').value = '';
        document.getElementById('delivery-memo').value = '선택 안함.';
        return;
    }

    // 선택한 배송지 정보로 폼 채우기
    document.getElementById('recipient-name').value = selectedOption.getAttribute('data-recipient');
    document.getElementById('tel').value = selectedOption.getAttribute('data-tel');
    document.getElementById('postcode').value = selectedOption.getAttribute('data-zipcode');
    document.getElementById('address').value = selectedOption.getAttribute('data-basic');
    document.getElementById('detail-address').value = selectedOption.getAttribute('data-detail');
    document.getElementById('delivery-memo').value = selectedOption.getAttribute('data-memo') || '선택 안함.';
}

function makeJsonDeliveryAddress() {
    const zipCode = $('#postcode').val();
    const recipientName = $('#recipient-name').val().trim();
    const basicAddress = $('#address').val();
    const detailAddress = $('#detail-address').val().trim();
    const tel = $('#tel').val();
    const memo = $('#delivery-memo').val();
    // 직접 입력 시 기본 별칭 생성 (주문_날짜시간)
    const alias = '주문_' + new Date().toLocaleString('ko-KR', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' }).replace(/\. /g, '-').replace(/:/g, '').replace(' ', '_');
    return JSON.stringify({alias, zipCode, recipientName, basicAddress, detailAddress, tel, memo});
}

function handleOrder() {
    const clientKey = document.getElementById("TOSS_CLIENT_KEY").value;     // Toss Payments Client Key (테스트 키)
    const customerName = document.getElementById("uid").value;
    const orderId = customerName + '_' + new Date().getTime();      // 고유 주문 ID
    const totalPayment = document.getElementById("totalPriceIncludingDeliveryCost").value.replace(/,/g, ""); // 총 결제 금액
    const successUrl = "http://localhost:8090/payment/success";     // 성공시 리다이렉트 URL
    const failureUrl = "http://localhost:8090/payment/failure";     // 실패시 리다이렉트 URL

    // 저장된 배송지 선택 확인
    const savedAddressSelect = document.getElementById('saved-address');
    const selectedAddressId = savedAddressSelect.value;

    if (selectedAddressId && selectedAddressId !== '') {
        // 1. 저장된 배송지를 선택한 경우 - 바로 결제 진행
        TossPayments(clientKey)
            .requestPayment('카드', {
                amount: parseInt(totalPayment),
                orderId: orderId,
                orderName: 'book_' + customerName + '_shopping',
                customerName: customerName,
                successUrl: successUrl + '?deliveryId=' + selectedAddressId,
                failUrl: failureUrl
            })
            .catch(function (error) {
                if (error.code === 'USER_CANCEL') {
                    alert('결제가 취소되었습니다.');
                } else {
                    alert('결제 중 오류가 발생했습니다: ' + error.message);
                }
            });
    } else {
        // 2. 직접 입력한 경우 - 배송지를 먼저 저장 후 결제 진행
        const deliveryAddress = makeJsonDeliveryAddress();

        // 필수 입력 검증
        const recipientName = document.getElementById('recipient-name').value.trim();
        const tel = document.getElementById('tel').value.trim();
        const zipCode = document.getElementById('postcode').value.trim();
        const basicAddress = document.getElementById('address').value.trim();
        const detailAddress = document.getElementById('detail-address').value.trim();

        if (!recipientName || !tel || !zipCode || !basicAddress || !detailAddress) {
            alert('배송지 정보를 모두 입력해주세요.');
            return;
        }

        // 배송지 저장 후 결제 진행
        fetch('/api/delivery-addresses', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: deliveryAddress
        })
        .then(response =>  {
            if (!response.ok) {
                throw new Error('배송지 저장 중 오류 발생');
            }
            return response.json();
        })
        .then(result => {
            const data = result.data; // OnBokResponse 구조
            console.log('배송지 저장 완료: ' + data.id);

            // Toss Payments 위젯 실행
            TossPayments(clientKey)
                .requestPayment('카드', {
                    amount: parseInt(totalPayment),
                    orderId: orderId,
                    orderName: 'book_' + customerName + '_shopping',
                    customerName: customerName,
                    successUrl: successUrl + '?deliveryId=' + data.id,
                    failUrl: failureUrl
                })
                .catch(function (error) {
                    if (error.code === 'USER_CANCEL') {
                        alert('결제가 취소되었습니다.');
                    } else {
                        alert('결제 중 오류가 발생했습니다: ' + error.message);
                    }
                });
        })
        .catch(error => {
            console.error("Error:", error);
            alert('배송지 저장 중 오류가 발생했습니다.');
        });
    }
}

function handleTelNumber(event) {
    const input = event.target;
    let value = input.value;

    // 숫자만 추출
    value = value.replace(/[^0-9]/g, "");

    // 전화번호 형식 적용
    if (value.length <= 10) {
        // 10자리: XXX-XXX-XXXX
        value = value.replace(/(\d{3})(\d{3})(\d{1,4})/, "$1-$2-$3");
    } else if (value.length <= 11) {
        // 11자리: XXX-XXXX-XXXX
        value = value.replace(/(\d{3})(\d{4})(\d{1,4})/, "$1-$2-$3");
    }

    // 값 갱신
    input.value = value;
}