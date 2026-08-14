document.addEventListener('DOMContentLoaded', function() {
    initializeCountdown();
    initializeUrgentNeedsRefresh();
    initializeEligibilityRing();
});

function initializeCountdown() {
    var eligibilityEl = document.getElementById('eligibility-card');
    if (!eligibilityEl) return;

    var eligible = eligibilityEl.getAttribute('data-eligible') === 'true';

    if (!eligible) {
        var nextEligibleDateStr = eligibilityEl.getAttribute('data-next-eligible-date');
        if (nextEligibleDateStr) {
            var nextDate = new Date(nextEligibleDateStr + 'T00:00:00');
            startCountdown(nextDate);
        }
    }
}

function startCountdown(endDate) {
    function updateCountdown() {
        var now = new Date();
        var diff = endDate - now;

        if (diff <= 0) {
            var countdownEl = document.getElementById('countdown-timer');
            if (countdownEl) {
                countdownEl.innerHTML =
                    '<div class="alert alert-success mb-0">' +
                    '<i class="fas fa-check-circle me-2"></i>You are now eligible to donate!' +
                    '</div>';
            }
            return;
        }

        var days = Math.floor(diff / (1000 * 60 * 60 * 24));
        var hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
        var minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
        var seconds = Math.floor((diff % (1000 * 60)) / 1000);

        var countdownHtml =
            '<div class="row g-2 justify-content-center">' +
            '<div class="col-3 col-md-2 col-lg-2 text-center">' +
                '<div class="countdown-item mx-auto">' +
                    '<span class="countdown-digit" id="countdown-days">' + padZero(days) + '</span>' +
                '</div>' +
                '<div class="countdown-label">Days</div>' +
            '</div>' +
            '<div class="col-3 col-md-2 col-lg-2 text-center">' +
                '<div class="countdown-item mx-auto">' +
                    '<span class="countdown-digit" id="countdown-hours">' + padZero(hours) + '</span>' +
                '</div>' +
                '<div class="countdown-label">Hours</div>' +
            '</div>' +
            '<div class="col-3 col-md-2 col-lg-2 text-center">' +
                '<div class="countdown-item mx-auto">' +
                    '<span class="countdown-digit" id="countdown-minutes">' + padZero(minutes) + '</span>' +
                '</div>' +
                '<div class="countdown-label">Minutes</div>' +
            '</div>' +
            '<div class="col-3 col-md-2 col-lg-2 text-center">' +
                '<div class="countdown-item mx-auto">' +
                    '<span class="countdown-digit" id="countdown-seconds">' + padZero(seconds) + '</span>' +
                '</div>' +
                '<div class="countdown-label">Seconds</div>' +
            '</div>' +
            '</div>';

        var countdownEl = document.getElementById('countdown-timer');
        if (countdownEl) {
            countdownEl.innerHTML = countdownHtml;
        }
    }

    updateCountdown();
    setInterval(updateCountdown, 1000);
}

function padZero(num) {
    return num < 10 ? '0' + num : num.toString();
}

function initializeUrgentNeedsRefresh() {
    var urgentContainer = document.getElementById('urgent-needs-container');
    if (!urgentContainer) return;

    setInterval(function() {
        fetch('/api/user/urgent-needs')
            .then(function(response) {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(function(data) {
                updateUrgentNeeds(data);
            })
            .catch(function(error) {
                console.error('Error fetching urgent needs:', error);
            });
    }, 60000);
}

function updateUrgentNeeds(data) {
    var container = document.getElementById('urgent-needs-container');
    if (!container) return;

    if (data.length === 0) {
        container.innerHTML =
            '<div class="alert alert-success mb-0 border-0">' +
            '<i class="fas fa-check-circle me-2"></i>No urgent needs currently matching your blood type in your city.' +
            '</div>';
        return;
    }

    var html = '<div class="row g-3">';
    for (var i = 0; i < data.length; i++) {
        var need = data[i];
        var urgencyBorderClass = need.urgency === 'CRITICAL' ? 'border-start border-4 border-danger' : 'border-start border-4 border-warning';

        html +=
            '<div class="col-md-6 col-lg-4">' +
                '<div class="card urgent-card h-100 shadow-sm border-0 ' + urgencyBorderClass + '">' +
                    '<div class="urgent-header">' +
                        '<div>' +
                            '<div class="urgent-hospital">' + need.hospitalName + '</div>' +
                            '<div class="urgent-date">' + need.requestedAt + '</div>' +
                        '</div>' +
                        '<span class="badge ' + (need.urgency === 'CRITICAL' ? 'urgency-critical' : 'urgency-high') + '">' + need.urgencyDisplayName + '</span>' +
                    '</div>' +
                    '<div class="urgent-body">' +
                        '<span class="urgent-type ' + (need.urgency === 'CRITICAL' ? 'red' : 'amber') + '">' + need.bloodTypeDisplayName + '</span>' +
                        '<small class="text-muted">blood needed</small>' +
                    '</div>' +
                '</div>' +
            '</div>';
    }
    html += '</div>';

    container.innerHTML = html;
}

function initializeEligibilityRing() {
    var progressCircles = document.querySelectorAll('.eligibility-ring-progress');
    progressCircles.forEach(function(circle) {
        var offset = circle.getAttribute('stroke-dashoffset');
        circle.style.strokeDashoffset = '439.82';
        setTimeout(function() {
            circle.style.strokeDashoffset = offset || '0';
        }, 100);
    });
}
