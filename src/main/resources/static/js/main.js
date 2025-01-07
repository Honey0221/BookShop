document.addEventListener('DOMContentLoaded', function () {
  // 중간 카테고리 로드 함수
  const loadMidCategories = function () {
    const mainCategories = document.querySelectorAll('.main-category-header');
    mainCategories.forEach(mainCategoryElement => {
      const mainCategory = mainCategoryElement.getAttribute('data-category');
      const midCategoriesContainer = mainCategoryElement.nextElementSibling;
      const midCategoriesList = midCategoriesContainer.querySelector('.mid-categories-list');

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

  // Swiper 설정 (기존 코드 유지)
  const swiperConfig = {
    slidesPerView: 5,
    centeredSlides: true,
    loop: true,
    spaceBetween: 30,
    speed: 400,
    pagination: {
      el: '.swiper-pagination',
      clickable: true,
    },
    navigation: {
      nextEl: '.swiper-button-next',
      prevEl: '.swiper-button-prev',
    },
    autoplay: {
      delay: 1000,
      disableOnInteraction: false,
      pauseOnMouseEnter: false,
      enabled: false
    },
    effect: 'coverflow',
    coverflowEffect: {
      rotate: 0,
      stretch: 0,
      depth: 100,
      modifier: 1,
      slideShadows: false,
    },
    breakpoints: {
      320: {
        slidesPerView: 1,
      },
      640: {
        slidesPerView: 2,
      },
      768: {
        slidesPerView: 3,
      },
      1024: {
        slidesPerView: 5,
      },
    },
    on: {
      click: function (swiper, event) {
        // 클릭된 슬라이드의 book-card를 찾아서 처리
        const clickedSlide = swiper.clickedSlide;
        if (clickedSlide) {
          const bookCard = clickedSlide.querySelector('.book-card');
          if (bookCard) {
            // 클릭한 상품의 상세 페이지로 이동
            const bookId = bookCard.dataset.bookId; // data-book-id 속성 필요
            window.location.href = `/item?bookId=${bookId}`;
          }
        }
      }
    }
  };

  // 기존 Swiper 초기화
  const newSwiper = new Swiper('.new-swiper', swiperConfig);
  const bestSwiper = new Swiper('.best-swiper', swiperConfig);

  // 새로운 추천 섹션 Swiper 초기화
  const personalizedSwiper = new Swiper('.personalized-swiper', swiperConfig);
  const collaborativeSwiper = new Swiper('.collaborative-swiper', swiperConfig);
  const contentBasedSwiper = new Swiper('.content-based-swiper', swiperConfig);

  // 기존 hover 이벤트
  const newContainer = document.querySelector('.new-swiper');
  newContainer.addEventListener('mouseenter', function () {
    newSwiper.autoplay.start();
  });
  newContainer.addEventListener('mouseleave', function () {
    newSwiper.autoplay.stop();
  });
  const bestContainer = document.querySelector('.best-swiper');
  bestContainer.addEventListener('mouseenter', function () {
    bestSwiper.autoplay.start();
  });
  bestContainer.addEventListener('mouseleave', function () {
    bestSwiper.autoplay.stop();
  });
  const personalizedContainer = document.querySelector('.personalized-swiper');
  personalizedContainer.addEventListener('mouseenter', function () {
    personalizedSwiper.autoplay.start();
  });
  personalizedContainer.addEventListener('mouseleave', function () {
    personalizedSwiper.autoplay.stop();
  });
  const collaborativeContainer = document.querySelector('.collaborative-swiper');
  collaborativeContainer.addEventListener('mouseenter', function () {
    collaborativeSwiper.autoplay.start();
  });
  collaborativeContainer.addEventListener('mouseleave', function () {
    collaborativeSwiper.autoplay.stop();
  });
  const contentBasedContainer = document.querySelector('.content-based-swiper');
  contentBasedContainer.addEventListener('mouseenter', function () {
    contentBasedSwiper.autoplay.start();
  });
  contentBasedContainer.addEventListener('mouseleave', function () {
    contentBasedSwiper.autoplay.stop();
  });
});