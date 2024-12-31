$(document).ready(function() {
  function getLastMonthRange() {
    const today = new Date();
    const lastMonth = new Date(today);
    lastMonth.setDate(today.getDate() - 29);

    const startDate = `${lastMonth.getMonth() + 1}.${lastMonth.getDate()}`;
    const endDate = `${today.getMonth() + 1}.${today.getDate()}`;

    return `${startDate} ~ ${endDate}`;
  }

  $('.range-item:nth-child(2) .range-date').text(getLastMonthRange());

  const ctx = $("#salesChart")[0].getContext("2d");
  let salesChart = new Chart(ctx, {
    type: "line",
    data: {
      labels: [],
      datasets: [{
        label: '일별 매출',
        data: [],
        borderColor: "#4e73df",
        tension: 0.1,
        fill: false
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
              return value.toLocaleString() + '원';
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
        }
      }
    }
  });

  // 기간 선택 버튼(7일, 30일)
  $(".period-btn").on("click", function() {
    $(".period-btn").removeClass("active");
    $(this).addClass("active");
    updateChart($(this).data("period"));
  });

  // 차트 데이터 업데이트
  function updateChart(days) {
    const labels = [];
    const data = [];
    const today = new Date();

    for (let i = days - 1; i >= 0; i--) {
      const date = new Date(today);
      date.setDate(date.getDate() - i);
      labels.push((date.getMonth() + 1) + '/' + date.getDate());
      // 임시 데이터
      data.push(Math.floor(Math.random() * 4000000) + 1000000);
    }

    salesChart.data.labels = labels;
    salesChart.data.datasets[0].data = data;
    salesChart.update();
  }

  updateChart(7);
});