const bookStatusData = {};
const categoryDistData = {};
const lowStockData = {};
const topViewedBooks = {};
const topViewedCategories = {};

$(document).ready(function() {
  const commonChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      title: {
        display: true,
        font: {
          size: 18,
          weight: 'bold'
        },
        padding: {
          top: 10,
          bottom: 20
        }
      },
      legend: {
        position: 'bottom',
        labels: {
          padding: 20,
          usePointStyle: true
        }
      }
    }
  };

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
          ...commonChartOptions,
          plugins: {
            ...commonChartOptions.plugins,
            title: {
              ...commonChartOptions.title,
              text: '도서 상태 분포'
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
          ...commonChartOptions,
          plugins: {
            ...commonChartOptions.plugins,
            title: {
              ...commonChartOptions.title,
              text: '카테고리별 도서 분포'
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
          ...commonChartOptions,
          plugins: {
            ...commonChartOptions.plugins,
            title: {
              ...commonChartOptions.title,
              text: '가격대별 도서 분포'
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
          ...commonChartOptions,
          plugins: {
            ...commonChartOptions.plugins,
            title: {
              ...commonChartOptions.title,
              text: '재고 부족 도서 Top 5'
            },
            tooltip: {
              callbacks: {
                label: function(context) {
                  return `${context.formattedValue}권`;
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
          ...commonChartOptions,
          plugins: {
            ...commonChartOptions.plugins,
            title: {
              ...commonChartOptions.title,
              text: '인기 도서 Top 3'
            },
            tooltip: {
              callbacks: {
                label: function(context) {
                  return `${context.formattedValue}회`;
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
          ...commonChartOptions,
          plugins: {
            ...commonChartOptions.plugins,
            title: {
              ...commonChartOptions.title,
              text: '인기 카테고리 Top 3'
            },
            tooltip: {
               callbacks: {
                   label: function(context) {
                       return `${context.formattedValue}회`;
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
    const button = $(this);
    const icon = button.find('i');

    button.prop('disabled', true);
    icon.addClass('fa-spin');

    $('.stat-card canvas').each(function() {
      const chartInstance = Chart.getChart(this);
      if (chartInstance) {
        chartInstance.destroy();
      }
    });

    loadAllCharts();

    setTimeout(() => {
      button.prop('disabled', false);
      icon.removeClass('fa-spin');
    }, 1000);
  });
});

function truncateString(str, maxLength) {
  return str.length > maxLength ? str.substring(0, maxLength) + '...' : str;
}