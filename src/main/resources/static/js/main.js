document.addEventListener('DOMContentLoaded', function () {
  const tabButtons = document.querySelectorAll('.tab-button');
  const tabPanes = document.querySelectorAll('.tab-pane');

  tabButtons.forEach(button => {
    button.addEventListener('click', () => {
      // 모든 탭 버튼에서 active 클래스 제거
      tabButtons.forEach(btn => btn.classList.remove('active'));
      // 모든 탭 패널에서 active 클래스 제거
      tabPanes.forEach(pane => pane.classList.remove('active'));

      // 클릭된 버튼과 해당 패널에 active 클래스 추가
      button.classList.add('active');
      const tabId = button.getAttribute('data-tab');
      document.getElementById(tabId).classList.add('active');

      // Swiper 업데이트 (필요한 경우)
      const swiper = document.querySelector(`#${tabId} .swiper`).swiper;
      if (swiper) {
        swiper.update();
      }
    });
  });

  // 중간 카테고리 로드 함수
  const loadMidCategories = function () {
    const mainCategories = document.querySelectorAll('.main-category-header');
    mainCategories.forEach(mainCategoryElement => {
      const mainCategory = mainCategoryElement.getAttribute('data-category');
      const midCategoriesContainer = mainCategoryElement.nextElementSibling;
      const midCategoriesList = midCategoriesContainer.querySelector('.mid-categories-list');

      // 메인 카테고리 클릭 이벤트 추가
      mainCategoryElement.addEventListener('click', function () {
        window.location.href = `/book-list/category?main=${encodeURIComponent(mainCategory)}`;
      });

      fetch(`/api/categories/${encodeURIComponent(mainCategory)}/mid`)
        .then(response => {
          if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
          }
          return response.json();
        })
        .then(midCategories => {
          const midCategoriesHtml = midCategories.map(midCat => `
            <li class="mid-category-item">
              <a href="/book-list/category?main=${encodeURIComponent(mainCategory)}&mid=${encodeURIComponent(midCat)}">
                ${midCat}
              </a>
            </li>
          `).join('');

          midCategoriesList.innerHTML = midCategoriesHtml;
        })
        .catch(error => {
          console.error('Error loading mid categories:', error);
        });
    });
  };

  // 페이지 로드 시 중간 카테고리 로드
  loadMidCategories();

  // 스와이퍼 공통 설정
  const commonSwiperConfig = {
    slidesPerView: 4,
    spaceBetween: 20,
    loop: true,
    pagination: {
      el: '.swiper-pagination',
      clickable: true,
      dynamicBullets: true
    },
    navigation: {
      nextEl: '.swiper-button-next',
      prevEl: '.swiper-button-prev'
    },
    autoplay: {
      delay: 2000,
      disableOnInteraction: false,
      pauseOnMouseEnter: true,
      enabled: false
    },
    breakpoints: {
      320: { slidesPerView: 1 },
      768: { slidesPerView: 2 },
      1024: { slidesPerView: 3 },
      1280: { slidesPerView: 4 }
    }
  };

  // 스와이퍼 초기화 함수
  function initializeSwiper(selector, config = {}) {
    return new Swiper(selector, { ...commonSwiperConfig, ...config });
  }

  // 책 카드 클릭 이벤트 처리
  function initializeBookCardClickEvents() {
    const bookCards = document.querySelectorAll('.book-card');
    bookCards.forEach(card => {
      card.addEventListener('click', function () {
        const bookId = this.getAttribute('data-book-id');
        if (bookId) {
          window.location.href = `/item?bookId=${bookId}`;
        }
      });

      // 커서 스타일 추가
      card.style.cursor = 'pointer';
    });
  }

  // 각 스와이퍼 초기화
  const swipers = {
    best: initializeSwiper('.best-swiper'),
    new: initializeSwiper('.new-swiper'),
    personalized: initializeSwiper('.personalized-swiper'),
    collaborative: initializeSwiper('.collaborative-swiper'),
    contentBased: initializeSwiper('.content-based-swiper')
  };

  // 각 스와이퍼가 초기화된 후 클릭 이벤트 등록
  Object.values(swipers).forEach(swiper => {
    swiper.on('init', function () {
      initializeBookCardClickEvents();
    });
  });

  // 초기 클릭 이벤트 등록
  initializeBookCardClickEvents();

  // 스와이퍼 마우스 이벤트 처리
  Object.entries(swipers).forEach(([key, swiper]) => {
    const container = document.querySelector(`.${key}-swiper`);
    if (container) {
      container.addEventListener('mouseenter', () => swiper.autoplay.start());
      container.addEventListener('mouseleave', () => swiper.autoplay.stop());
    }
  });

  // 챗봇 관련 요소 선택
  const aiChatIcon = document.querySelector('.chat-bot-icon');
  const aiChatModal = document.querySelector('.chat-modal');
  const aiChatClose = document.querySelector('.chat-close');
  const aiChatInput = document.querySelector('.chat-input');
  const aiChatSend = document.querySelector('.chat-send');
  const aiChatMessages = document.querySelector('.chat-messages');

  // 챗봇 모달 토글
  aiChatIcon.addEventListener('click', () => {
    aiChatModal.style.display = 'block';
    if (aiChatMessages.children.length === 0) {
      addBotMessage('안녕하세요! 도서 추천 챗봇입니다. 어떤 장르의 책을 찾으시나요? 소설, 자기계발, 과학, 기술 등 관심 있는 분야를 알려주세요.');
    }
  });

  aiChatClose.addEventListener('click', () => {
    aiChatModal.style.display = 'none';
  });

  // 메시지 전송 처리
  function sendMessage() {
    const message = aiChatInput.value.trim();
    if (message) {
      addUserMessage(message);
      aiChatInput.value = '';

      // 서버로 메시지 전송
      fetch('/api/ai-chat/message', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          message: message,
          userId: null  // 필요한 경우 사용자 ID 추가
        })
      })
        .then(response => response.json())
        .then(data => {
          // 챗봇 응답 메시지 표시
          addBotMessage(data.message);

          // 책 추천 목록이 있는 경우 표시
          if (data.recommendations && data.recommendations.length > 0) {
            const recommendationsHtml = data.recommendations.map(book => `
              <div class="book-recommendation" data-book-id="${book.bookId}">
                <img src="${book.imageUrl}" alt="${book.title}" class="book-thumb">
                <div class="book-info">
                  <div class="book-title">${book.title}</div>
                  <div class="book-price">${book.price.toLocaleString()}원</div>
                </div>
              </div>
            `).join('');

            const recommendationsContainer = document.createElement('div');
            recommendationsContainer.className = 'message bot-message recommendations';
            recommendationsContainer.innerHTML = recommendationsHtml;

            // 추천 카드 클릭 이벤트 추가
            recommendationsContainer.querySelectorAll('.book-recommendation').forEach(card => {
              card.addEventListener('click', function () {
                const bookId = this.getAttribute('data-book-id');
                if (bookId) {
                  window.location.href = `/item?bookId=${bookId}`;
                }
              });
            });

            aiChatMessages.appendChild(recommendationsContainer);
          }

          aiChatMessages.scrollTop = aiChatMessages.scrollHeight;
        })
        .catch(error => {
          console.error('Error:', error);
          addBotMessage('죄송합니다. 일시적인 오류가 발생했습니다.');
        });
    }
  }

  aiChatSend.addEventListener('click', sendMessage);
  aiChatInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
      sendMessage();
    }
  });

  // 메시지 추가 함수들
  function addUserMessage(text) {
    const message = document.createElement('div');
    message.className = 'message user-message';
    message.textContent = text;
    aiChatMessages.appendChild(message);
    aiChatMessages.scrollTop = aiChatMessages.scrollHeight;
  }

  function addBotMessage(text) {
    const message = document.createElement('div');
    message.className = 'message bot-message';
    message.textContent = text;
    aiChatMessages.appendChild(message);
    aiChatMessages.scrollTop = aiChatMessages.scrollHeight;
  }

  // 1:1 채팅 관련 요소 선택
  const chatServiceIcon = document.querySelector('.chat-service-icon');
  const chatServiceModal = document.querySelector('.chat-service-modal');
  const chatServiceClose = document.querySelector('.chat-service-close');
  const chatServiceInput = document.querySelector('.chat-service-input');
  const chatServiceSend = document.querySelector('.chat-service-send');
  const chatServiceMessages = document.querySelector('.chat-service-messages');

  // 1:1 채팅 모달 토글
  chatServiceIcon.addEventListener('click', () => {
    chatServiceModal.style.display = 'block';

    if (!stompClient) {
      connectWebSocket();

    } else if (currentRoomId) {
      // 채팅방이 있는 경우 채팅 기록 로드
      loadChatHistory(currentRoomId);

      // 채팅방 재구독
      stompClient.subscribe('/queue/chat.' + currentRoomId, function (message) {
        const received = JSON.parse(message.body);
        displayMessage(received);
      }, { id: 'sub-' + currentRoomId });
    }
  });

  // 모달 닫기 이벤트
  chatServiceClose.addEventListener('click', () => {
    chatServiceModal.style.display = 'none';

    // 현재 구독 중인 채팅방 구독 해제
    if (currentRoomId) {
      stompClient.unsubscribe('sub-' + currentRoomId);
    }
  });

  // 1:1 채팅 메시지 전송 처리
  function sendServiceMessage() {
    const message = chatServiceInput.value.trim();
    if (message && currentRoomId) {  // currentRoomId 존재 여부 확인
      const chatMessage = {
        type: 'MESSAGE',
        roomId: currentRoomId,
        sender: 'USER',
        message: message
      };

      stompClient.send("/app/chat.message", {}, JSON.stringify(chatMessage));
      chatServiceInput.value = '';

      // 사용자 메시지 즉시 표시
      //addServiceMessage(message, 'user');
    } else {
      console.error('No active chat room');
      // 선택적: 사용자에게 채팅방이 없다는 알림 표시
      alert('채팅방이 선택되지 않았습니다.');
    }
  }

  // 1:1 채팅 메시지 추가 함수
  function addServiceMessage(text, type) {
    const message = document.createElement('div');
    message.className = `message ${type}-message`;
    message.textContent = text;
    chatServiceMessages.appendChild(message);
    chatServiceMessages.scrollTop = chatServiceMessages.scrollHeight;
  }

  // 1:1 채팅 이벤트 리스너
  chatServiceSend.addEventListener('click', sendServiceMessage);
  chatServiceInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
      sendServiceMessage();
    }
  });

  // WebSocket 연결 및 채팅 관련 코드
  let stompClient = null;
  let currentRoomId = null;  // 전역 변수로 선언

  function connectWebSocket() {
    const socket = new SockJS('/ws-chat');
    stompClient = Stomp.over(socket);
    const token = $("meta[name='_csrf']").attr("content");
    const header = $("meta[name='_csrf_header']").attr("content");

    const headers = {
      [header]: token
    };

    stompClient.connect(headers, function (frame) {
      console.log('Connected: ' + frame);

      // 채팅방 생성 요청
      fetch('/chat/room', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          [header]: token
        }
      })
        .then(response => response.json())
        .then(room => {
          currentRoomId = room.roomId;  // 채팅방 ID 저장

          // 채팅방 구독
          stompClient.subscribe('/queue/chat.' + currentRoomId, function (message) {
            const received = JSON.parse(message.body);
            displayMessage(received);
          });

          // 채팅 기록 로드
          loadChatHistory(currentRoomId);
          addServiceMessage('안녕하세요! 1:1 문의 채팅입니다. 어떤 도움이 필요하신가요?', 'admin');
        })
        .catch(error => {
          console.error('Error creating chat room:', error);
        });
    });
  }

  function displayMessage(received) {
    const message = document.createElement('div');
    message.className = `message ${received.sender.toLowerCase()}-message`;

    // 시간 포맷팅
    const messageTime = received.time || new Date().toLocaleTimeString('ko-KR', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: false
    });

    message.innerHTML = `
        <div class="message-content">${received.message}</div>
        <div class="message-time">${messageTime}</div>
    `;

    chatServiceMessages.appendChild(message);
    chatServiceMessages.behavior = 'smooth';
    chatServiceMessages.scrollTo(0, chatServiceMessages.scrollHeight);

    //chatServiceMessages.scrol lTop = chatServiceMessages.scrollHeight;

    // 관리자 메시지가 아니고, 현재 채팅방이 열려있지 않은 경우에만 알림 처리
    if (received.sender !== 'ADMIN' && chatServiceModal.style.display !== 'block') {
      // 알림 뱃지 표시
      const chatIcon = document.querySelector('.chat-service-icon');
      let badge = chatIcon.querySelector('.notification-badge');
      if (!badge) {
        badge = document.createElement('span');
        badge.className = 'notification-badge';
        chatIcon.appendChild(badge);
      }
      badge.style.display = 'block';
    }
  }

  // 채팅 기록 로드 함수
  function loadChatHistory(roomId) {
    if (!roomId) return; // roomId가 없으면 함수 종료

    const token = $("meta[name='_csrf']").attr("content");
    const header = $("meta[name='_csrf_header']").attr("content");

    fetch(`/chat/messages/${roomId}`, {
      headers: {
        [header]: token
      }
    })
      .then(response => response.json())
      .then(messages => {
        // 기존 메시지 클리어
        chatServiceMessages.innerHTML = '';

        // 메시지 기록 표시
        messages.forEach(message => {
          displayMessage(message);
        });

        // 스크롤을 최하단으로
        chatServiceMessages.scrollTop = chatServiceMessages.scrollHeight;
      })
      .catch(error => {
        console.error('Error loading chat history:', error);
      });
  }

  // 메시지 전송 이벤트 수정
  chatServiceSend.addEventListener('click', sendMessage);
  chatServiceInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
      sendMessage();
    }
  });
});
