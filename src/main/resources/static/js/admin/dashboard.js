let salesChart;

$(document).ready(function() {
  // 날짜 정보 적용
  const dateInfo = getLastMonthRange();
  $('.range-item:nth-child(1) .range-date').text(dateInfo.minDate);
  $('.range-item:nth-child(2) .range-date').text(dateInfo.maxDate);
  $('.range-item:nth-child(3) .range-date').text(dateInfo.period);

  // 차트 초기화
  const ctx = $("#salesChart")[0].getContext("2d");
  salesChart = new Chart(ctx, {
    type: "line",
    data: {
      labels: [],
      datasets: [{
        label: '일별 매출액',
        data: [],
        borderColor: "#4e73df",
        backgroundColor: "#4e73df",
        tension: 0.1,
        fill: false,
        pointRadius: 4,
        pointHoverRadius: 6,
        pointBackgroundColor: "#fff",
        pointBorderWidth: 2,
        pointHoverBorderWidth: 3,
        pointHoverBackgroundColor: "#fff",
        pointHoverBorderColor: "#4e74df",
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        y: {
          beginAtZero: true,
          ticks: {
            callback: function(value) {
              return value === 0 ? '' : value.toLocaleString();
            },
            stepSize: 1000000,
            font: {
              size: 12
            },
            padding: 10
          },
          grid: {
            drawBorder: false,
            color: "#e9ecef"
          }
        },
        x: {
          grid: {
            display: false
          },
          ticks: {
            font: {
              size: 12
            }
          }
        }
      },
      plugins: {
        tooltip: {
          callbacks: {
            label: function(context) {
              return context.parsed.y.toLocaleString() + '원';
            }
          }
        },
        legend: {
          labels: {
            font: {
              size: 14
            }
          }
        }
      },
      interaction: {
        intersect: false,
        mode: 'index'
      }
    }
  });

  // 기간 선택 버튼 이벤트
  $(".period-btn").on("click", function() {
    $(".period-btn").removeClass("active");
    $(this).addClass("active");
    const period = $(this).data("period");
    updateDashBoard(period);
  });

  // 초기 차트 데이터 로드
  updateDashBoard(7);
});

// 랜덤 날짜 생성 함수
function getRandomDate(start, end) {
  const startDate = new Date(start);
  const endDate = new Date(end);
  const randomTime = startDate.getTime() + Math.random() * (endDate.getTime() - startDate.getTime());
  const randomDate = new Date(randomTime);
  return `${randomDate.getMonth() + 1}.${randomDate.getDate()}`;
}

// 최근 30일 범위 구하는 함수
function getLastMonthRange() {
  const today = new Date();
  const lastMonth = new Date(today);
  lastMonth.setDate(today.getDate() - 29);

  // 최저, 최고 매출일 랜덤 설정
  const minDate = getRandomDate(lastMonth, today);
  const maxDate = getRandomDate(lastMonth, today);

  // 기간 표시용 텍스트
  const startDate = `${lastMonth.getMonth() + 1}.${lastMonth.getDate()}`;
  const endDate = `${today.getMonth() + 1}.${today.getDate()}`;
  const periodText = `${startDate} ~ ${endDate}`;

  return {
    period: periodText,
    minDate: minDate,
    maxDate: maxDate
  };
}

// 대시보드 전체 업데이트 함수
function updateDashBoard(days) {
  const salesData = generateSalesData(days);
  updateChart(days, salesChart, salesData);
  updateTodayStats(salesData);
  updateSalesRange(salesData);
}

// 매출 데이터 생성 함수
function generateSalesData(days) {
  const data = [];
  const today = new Date();

  for (let i = days - 1; i >= 0; i--) {
    const date = new Date(today);
    date.setDate(date.getDate() - i);
    const randomSales = Math.floor((Math.random() * 4000000 + 1000000) / 100) * 100;
    data.push({
      date: (date.getMonth() + 1) + '/' + date.getDate(),
      sales: randomSales
    });
  }

  return data;
}

// 차트 데이터 업데이트 함수
function updateChart(days, salesChart, salesData) {
  const labels = salesData.map(item => item.date);
  const data = salesData.map(item => item.sales);

  salesChart.data.labels = labels;
  salesChart.data.datasets[0].data = data;
  salesChart.update();
}

// 오늘의 매출 통계 업데이트 함수
function updateTodayStats(salesData) {
  const today = salesData[salesData.length - 1].sales;
  const yesterday = salesData[salesData.length - 2].sales;
  const difference = today - yesterday;
  const isIncrease = difference > 0;

  // 오늘의 매출액 업데이트
  $('.stat-value').text(today.toLocaleString() + '원');

  // 전일 대비 차이 업데이트
  const compareElement = $('.stat-compare');
  compareElement.html(`
    <span class="${isIncrease ? 'increase' : 'decrease'}">
      <span class="compare-text">전일 대비</span>
      ${Math.abs(difference).toLocaleString()}원
      <i class="fas fa-arrow-${isIncrease ? 'up' : 'down'}"></i>
    </span>
  `);
}

// 매출 범위 통계 업데이트 함수
function updateSalesRange(salesData) {
  const sales = salesData.map(item => item.sales);
  const minSale = Math.min(...sales);
  const maxSale = Math.max(...sales);
  const avgSale = Math.floor(sales.reduce((a, b) => a + b, 0) / sales.length / 100) * 100;

  // 최저 매출
  $('#minSales').text(minSale.toLocaleString() + '원');
  $('#minSalesDate').text(salesData.find(item => item.sales === minSale).date);

  // 최고 매출
  $('#maxSales').text(maxSale.toLocaleString() + '원');
  $('#maxSalesDate').text(salesData.find(item => item.sales === maxSale).date);

  // 평균 매출
  $('#avgSales').text(avgSale.toLocaleString() + '원');
  $('#salesPeriod').text(getLastMonthRange().period);
}