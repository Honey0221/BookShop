const bookStatusData = {};
const categoryDistData = {};
const lowStockData = {};
const topViewedBooks = {};
const topViewedCategories = {};

$(document).ready(function() {
  // 모든 차트를 로드하는 함수
  function loadAllCharts() {
    loadStatusChart();
    loadCategoryChart();
    loadPriceRangeChart();
    loadLowStockChart();
    loadTopViewedBooksChart();
    loadTopViewedCategoriesChart();
  }
  // 도서 상태 분포
  function loadStatusChart() {
    $.get('/admin/stats/status', function(data) {
      new Chart($('#statusChart'), {
        type: 'doughnut',
        data: {
          labels: ['판매중', '품절'],
          datasets: [{
            data: [data.selling, data.soldOut],
            backgroundColor: ['#36A2EB', '#ff6384']
          }]
        },
        options: {
          responsive: true,
          plugins: {
            title: {
              display: true,
              text: '도서 상태 분포',
              font: {
                size: 24,
                weight: 'bold'
              },
              padding: 20
            }
          }
        }
      });
    });
  }

  // 중분류별 도서 수 분포
  function loadCategoryChart() {
    $.get('/admin/stats/category', function(data) {
      const colors = [
        '#FF9999', '#66B2FF', '#99FF99', '#FFCC99', '#FF99CC', '#99CCFF', '#FFB366',
        '#FF99FF', '#99FFCC', '#FFE5CC', '#B399FF', '#99E6FF', '#FFB3B3', '#99FFE6'
      ];
      new Chart($('#categoryChart'), {
        type: 'pie',
        data: {
          labels: data.labels,
          datasets: [{
            data: data.data,
            backgroundColor: data.labels.map((_, index) =>
              data.labels[index] === '기타' ? '#808080' : colors[index % colors.length]
            )
          }]
        },
        options: {
          responsive: true,
          plugins: {
            title: {
              display: true,
              text: '카테고리별 도서 분포',
              font: {
                size: 24,
                weight: 'bold'
              },
              padding: 20
            }
          }
        }
      });
    });
  }

  // 가격대별 도서 분포
  function loadPriceRangeChart() {
    $.get('/admin/stats/price-range', function(data) {
      new Chart($('#priceRangeChart'), {
        type: 'bar',
        data: {
          labels: data.labels,
          datasets: [{
            label: '도서 수',
            data: data.data,
            backgroundColor: '#4BC0C0'
          }]
        },
        options: {
          responsive: true,
          plugins: {
            title: {
              display: true,
              text: '가격대별 도서 분포',
              font: {
                size: 24,
                weight: 'bold'
              },
              padding: 20
            },
            tooltip: {
              callbacks: {
                label: function(context) {
                  return `${context.label}: ${context.formattedValue}권`;
                }
              }
            }
          }
        }
      });
    });
  }

  // 재고 적은 도서 Top 5
  function loadLowStockChart() {
    $.get('/admin/stats/low-stock', function(data) {
      const truncatedLabels = data.labels.map(label => truncateString(label, 7));
      new Chart($('#lowStockChart'), {
        type: 'bar',
        data: {
          labels: truncatedLabels,
          datasets: [{
            label: '재고 수량',
            data: data.data,
            backgroundColor: '#FF6384'
          }]
        },
        options: {
          responsive: true,
          plugins: {
            title: {
              display: true,
              text: '재고 부족 도서 Top 5',
              font: {
                size: 24,
                weight: 'bold'
              },
              padding: 20
            },
            tooltip: {
              callbacks: {
                label: function(context) {
                  return `${data.labels[context.dataIndex]}: ${context.formattedValue}권`;
                }
              }
            }
          }
        }
      });
    });
  }

  // 조회수 높은 도서 Top 3
  function loadTopViewedBooksChart() {
    $.get('/admin/stats/top-viewed-books', function(data) {
      const truncatedLabels = data.labels.map(label => truncateString(label, 7));
      new Chart($('#topViewedBooksChart'), {
        type: 'bar',
        data: {
          labels: truncatedLabels,
          datasets: [{
            label: '조회수',
            data: data.data,
            backgroundColor: '#36A2EB'
          }]
        },
        options: {
          indexAxis: 'y',
          responsive: true,
          plugins: {
            title: {
              display: true,
              text: '인기 도서 Top 3',
              font: {
                size: 24,
                weight: 'bold'
              },
              padding: 20
            },
            tooltip: {
              callbacks: {
                label: function(context) {
                  return `${data.labels[context.dataIndex]}: ${context.formattedValue}회`;
                }
              }
            }
          }
        }
      });
    });
  }

  // 조회수 높은 중분류 Top 3
  function loadTopViewedCategoriesChart() {
    $.get('/admin/stats/top-viewed-categories', function(data) {
      new Chart($('#topViewedCategoriesChart'), {
        type: 'bar',
        data: {
          labels: data.labels,
          datasets: [{
            label: '총 조회수',
            data: data.data,
            backgroundColor: '#FFCE56'
          }]
        },
        options: {
          indexAxis: 'y',
          responsive: true,
          plugins: {
            title: {
              display: true,
              text: '인기 카테고리 Top 3',
              font: {
                size: 24,
                weight: 'bold'
              },
              padding: 20
            },
            tooltip: {
               callbacks: {
                   label: function(context) {
                       return `${context.label}: ${context.formattedValue}회`;
                   }
               }
            }
          }
        }
      });
    });
  }

  loadAllCharts();

  $('#refreshStats').click(function() {
    const icon = $(this).find('i');
    icon.addClass('fa-spin');

    $('.chart-container canvas').each(function() {
      const chartInstance = Chart.getChart(this);
      if (chartInstance) {
        chartInstance.destroy();
      }
    });

    loadAllCharts();

    setTimeout(() => {
      icon.removeClass('fa-spin');
    }, 1000);
  });
});

function truncateString(str, maxLength) {
  return str.length > maxLength ? str.substring(0, maxLength) + '...' : str;
}