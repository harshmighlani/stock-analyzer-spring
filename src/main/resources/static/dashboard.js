// Dashboard JavaScript functionality
class StockDashboard {
    constructor() {
        this.apiBaseUrl = '';
        this.recommendationChart = null;
        this.riskChart = null;
        this.init();
    }

    init() {
        this.bindEvents();
        this.loadDashboardData();
        this.loadRecommendations();
    }

    bindEvents() {
        // Refresh button
        document.getElementById('refreshBtn').addEventListener('click', () => {
            this.loadDashboardData();
            this.loadRecommendations();
        });

        // Trigger analysis button
        document.getElementById('triggerAnalysisBtn').addEventListener('click', () => {
            this.triggerAnalysis();
        });

        // Analyze individual stock
        document.getElementById('analyzeBtn').addEventListener('click', () => {
            this.analyzeStock();
        });

        // Filter recommendations
        document.getElementById('filterRecommendation').addEventListener('change', (e) => {
            this.filterRecommendations(e.target.value);
        });

        // Enter key for stock analysis
        document.getElementById('stockSymbol').addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                this.analyzeStock();
            }
        });
    }

    async loadDashboardData() {
        try {
            this.showLoading();
            const response = await fetch('/api/automation/dashboard');
            const data = await response.json();

            this.updateStatusCards(data);
            this.updateCharts(data);

        } catch (error) {
            console.error('Error loading dashboard data:', error);
            this.showToast('Error loading dashboard data', 'error');
        } finally {
            this.hideLoading();
        }
    }

    async loadRecommendations() {
        try {
            const response = await fetch('/api/automation/recommendations');
            const recommendations = await response.json();

            this.updateRecommendationsTable(recommendations);

        } catch (error) {
            console.error('Error loading recommendations:', error);
            this.showToast('Error loading recommendations', 'error');
        }
    }

    updateStatusCards(data) {
        document.getElementById('totalAnalyzed').textContent = data.totalStocksAnalyzed || 0;
        document.getElementById('lastUpdate').textContent = this.formatDateTime(data.lastUpdate);

        // Count strong buys
        const strongBuys = data.topRecommendations ? data.topRecommendations.length : 0;
        document.getElementById('strongBuys').textContent = strongBuys;

        // Count high risk stocks
        const highRisk = data.riskDistribution ? (data.riskDistribution['High Risk'] || 0) : 0;
        document.getElementById('highRisk').textContent = highRisk;
    }

    updateCharts(data) {
        this.createRecommendationChart(data);
        this.createRiskChart(data);
    }

    createRecommendationChart(data) {
        const ctx = document.getElementById('recommendationChart').getContext('2d');

        // Destroy existing chart
        if (this.recommendationChart) {
            this.recommendationChart.destroy();
        }

        // Sample data - in real implementation, this would come from the API
        const chartData = {
            labels: ['Strong Buy', 'Buy', 'Hold', 'Sell', 'Strong Sell'],
            datasets: [{
                data: [12, 19, 8, 3, 2],
                backgroundColor: [
                    '#48bb78',
                    '#4299e1',
                    '#ed8936',
                    '#f56565',
                    '#e53e3e'
                ],
                borderWidth: 0
            }]
        };

        this.recommendationChart = new Chart(ctx, {
            type: 'doughnut',
            data: chartData,
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            padding: 20,
                            usePointStyle: true
                        }
                    }
                }
            }
        });
    }

    createRiskChart(data) {
        const ctx = document.getElementById('riskChart').getContext('2d');

        // Destroy existing chart
        if (this.riskChart) {
            this.riskChart.destroy();
        }

        const riskData = data.riskDistribution || { 'Low Risk': 0, 'Medium Risk': 0, 'High Risk': 0 };

        const chartData = {
            labels: Object.keys(riskData),
            datasets: [{
                data: Object.values(riskData),
                backgroundColor: [
                    '#48bb78',
                    '#ed8936',
                    '#f56565'
                ],
                borderWidth: 0
            }]
        };

        this.riskChart = new Chart(ctx, {
            type: 'pie',
            data: chartData,
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            padding: 20,
                            usePointStyle: true
                        }
                    }
                }
            }
        });
    }

    updateRecommendationsTable(recommendations) {
        const tbody = document.getElementById('recommendationsTableBody');

        if (!recommendations || recommendations.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="7" class="text-center">
                        <i class="fas fa-info-circle"></i>
                        No recommendations available
                    </td>
                </tr>
            `;
            return;
        }

        tbody.innerHTML = recommendations.map(rec => `
            <tr>
                <td><strong>${rec.symbol}</strong></td>
                <td>${rec.companyName || 'N/A'}</td>
                <td>$${rec.currentPrice ? rec.currentPrice.toFixed(2) : 'N/A'}</td>
                <td>$${rec.targetPrice ? rec.targetPrice.toFixed(2) : 'N/A'}</td>
                <td>
                    <span class="recommendation-badge badge-${rec.recommendation.toLowerCase().replace('_', '-')}">
                        ${rec.recommendation.replace('_', ' ')}
                    </span>
                </td>
                <td>
                    <span class="risk-level risk-${this.getRiskLevelClass(rec.riskLevel)}">
                        <i class="fas fa-shield-alt"></i>
                        ${this.getRiskLevelText(rec.riskLevel)}
                    </span>
                </td>
                <td>
                    <button class="btn btn-sm btn-secondary" onclick="dashboard.showRecommendationDetails('${rec.symbol}')">
                        <i class="fas fa-eye"></i>
                        View Details
                    </button>
                </td>
            </tr>
        `).join('');
    }

    async analyzeStock() {
        const symbol = document.getElementById('stockSymbol').value.trim().toUpperCase();

        if (!symbol) {
            this.showToast('Please enter a stock symbol', 'warning');
            return;
        }

        try {
            this.showLoading();
            const response = await fetch(`/analyze?symbol=${symbol}`);
            const data = await response.json();

            this.displayAnalysisResult(data);
            this.showToast(`Analysis completed for ${symbol}`, 'success');

        } catch (error) {
            console.error('Error analyzing stock:', error);
            this.showToast(`Error analyzing ${symbol}`, 'error');
        } finally {
            this.hideLoading();
        }
    }

    displayAnalysisResult(data) {
        const resultDiv = document.getElementById('analysisResult');
        resultDiv.classList.remove('hidden');

        resultDiv.innerHTML = `
            <h4><i class="fas fa-chart-line"></i> Analysis Results</h4>
            <div class="analysis-details">
                <div class="detail-row">
                    <strong>Symbol:</strong> ${data.symbol || 'N/A'}
                </div>
                <div class="detail-row">
                    <strong>Company:</strong> ${data.companyName || 'N/A'}
                </div>
                <div class="detail-row">
                    <strong>Current Price:</strong> $${data.currentPrice ? data.currentPrice.toFixed(2) : 'N/A'}
                </div>
                <div class="detail-row">
                    <strong>Recommendation:</strong> 
                    <span class="recommendation-badge badge-${data.recommendation ? data.recommendation.toLowerCase().replace('_', '-') : 'hold'}">
                        ${data.recommendation ? data.recommendation.replace('_', ' ') : 'HOLD'}
                    </span>
                </div>
                <div class="detail-row">
                    <strong>Reasoning:</strong> ${data.reasoning || 'No reasoning provided'}
                </div>
            </div>
        `;
    }

    async triggerAnalysis() {
        try {
            this.showLoading();
            const response = await fetch('/api/automation/trigger-analysis', {
                method: 'POST'
            });
            const data = await response.json();

            if (data.status === 'success') {
                this.showToast('Analysis triggered successfully', 'success');
                // Refresh data after a short delay
                setTimeout(() => {
                    this.loadDashboardData();
                    this.loadRecommendations();
                }, 2000);
            } else {
                this.showToast(data.message || 'Failed to trigger analysis', 'error');
            }

        } catch (error) {
            console.error('Error triggering analysis:', error);
            this.showToast('Error triggering analysis', 'error');
        } finally {
            this.hideLoading();
        }
    }

    filterRecommendations(filter) {
        const rows = document.querySelectorAll('#recommendationsTableBody tr');

        rows.forEach(row => {
            if (filter === 'all') {
                row.style.display = '';
            } else {
                const recommendationCell = row.querySelector('.recommendation-badge');
                if (recommendationCell) {
                    const recommendation = recommendationCell.textContent.trim().replace(' ', '_').toUpperCase();
                    row.style.display = recommendation === filter ? '' : 'none';
                }
            }
        });
    }

    showRecommendationDetails(symbol) {
        // This would open a modal or navigate to a detailed view
        this.showToast(`Viewing details for ${symbol}`, 'info');
    }

    getRiskLevelClass(riskLevel) {
        if (riskLevel <= 3) return 'low';
        if (riskLevel <= 6) return 'medium';
        return 'high';
    }

    getRiskLevelText(riskLevel) {
        if (riskLevel <= 3) return 'Low';
        if (riskLevel <= 6) return 'Medium';
        return 'High';
    }

    formatDateTime(dateTimeString) {
        if (!dateTimeString || dateTimeString === 'No data available') {
            return 'Never';
        }

        try {
            const date = new Date(dateTimeString);
            return date.toLocaleString();
        } catch (error) {
            return dateTimeString;
        }
    }

    showLoading() {
        document.getElementById('loadingOverlay').classList.remove('hidden');
    }

    hideLoading() {
        document.getElementById('loadingOverlay').classList.add('hidden');
    }

    showToast(message, type = 'info') {
        const container = document.getElementById('toastContainer');
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;

        const icon = this.getToastIcon(type);
        toast.innerHTML = `
            <i class="fas ${icon}"></i>
            <span>${message}</span>
        `;

        container.appendChild(toast);

        // Auto remove after 5 seconds
        setTimeout(() => {
            toast.remove();
        }, 5000);
    }

    getToastIcon(type) {
        switch (type) {
            case 'success': return 'fa-check-circle';
            case 'error': return 'fa-exclamation-circle';
            case 'warning': return 'fa-exclamation-triangle';
            default: return 'fa-info-circle';
        }
    }
}

// Initialize dashboard when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.dashboard = new StockDashboard();
});

// Add some additional CSS for analysis details
const additionalStyles = `
.analysis-details {
    margin-top: 1rem;
}

.detail-row {
    margin-bottom: 0.75rem;
    padding: 0.5rem 0;
    border-bottom: 1px solid #e2e8f0;
}

.detail-row:last-child {
    border-bottom: none;
}

.btn-sm {
    padding: 0.5rem 1rem;
    font-size: 0.8rem;
}

.recommendation-badge {
    font-size: 0.75rem;
    padding: 0.2rem 0.6rem;
}

.toast.info {
    border-left: 4px solid #4299e1;
}
`;

// Inject additional styles
const styleSheet = document.createElement('style');
styleSheet.textContent = additionalStyles;
document.head.appendChild(styleSheet);
