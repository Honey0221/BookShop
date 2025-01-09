document.addEventListener('DOMContentLoaded', function () {
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
  const chatIcon = document.querySelector('.chat-bot-icon');
  const chatModal = document.querySelector('.chat-modal');
  const chatClose = document.querySelector('.chat-close');
  const chatInput = document.querySelector('.chat-input');
  const chatSend = document.querySelector('.chat-send');
  const chatMessages = document.querySelector('.chat-messages');

  // 챗봇 모달 토글
  chatIcon.addEventListener('click', () => {
    chatModal.style.display = 'block';
    if (chatMessages.children.length === 0) {
      addBotMessage('안녕하세요! 도서 추천 챗봇입니다. 어떤 장르의 책을 찾으시나요? 소설, 자기계발, 과학, 기술 등 관심 있는 분야를 알려주세요.');
    }
  });

  chatClose.addEventListener('click', () => {
    chatModal.style.display = 'none';
  });

  // 메시지 전송 처리
  function sendMessage() {
    const message = chatInput.value.trim();
    if (message) {
      addUserMessage(message);
      chatInput.value = '';

      // 서버로 메시지 전송
      fetch('/api/chat/message', {
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

            chatMessages.appendChild(recommendationsContainer);
          }

          chatMessages.scrollTop = chatMessages.scrollHeight;
        })
        .catch(error => {
          console.error('Error:', error);
          addBotMessage('죄송합니다. 일시적인 오류가 발생했습니다.');
        });
    }
  }

  chatSend.addEventListener('click', sendMessage);
  chatInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
      sendMessage();
    }
  });

  // 메시지 추가 함수들
  function addUserMessage(text) {
    const message = document.createElement('div');
    message.className = 'message user-message';
    message.textContent = text;
    chatMessages.appendChild(message);
    chatMessages.scrollTop = chatMessages.scrollHeight;
  }

  function addBotMessage(text) {
    const message = document.createElement('div');
    message.className = 'message bot-message';
    message.textContent = text;
    chatMessages.appendChild(message);
    chatMessages.scrollTop = chatMessages.scrollHeight;
  }
});
