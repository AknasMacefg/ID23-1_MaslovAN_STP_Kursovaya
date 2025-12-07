// Simple carousel functionality
function moveCarousel(carouselId, direction) {
    const carousel = document.getElementById(carouselId);
    if (!carousel) return;
    
    const track = carousel.querySelector('.carousel-track');
    if (!track) return;
    
    const cardWidth = 220; // 200px card + 20px gap
    const scrollAmount = cardWidth * 3; // Scroll 3 cards at a time
    
    const currentScroll = track.style.transform 
        ? parseInt(track.style.transform.match(/-?\d+/)[0]) || 0 
        : 0;
    
    const newScroll = currentScroll + (direction * scrollAmount);
    const maxScroll = -(track.scrollWidth - carousel.offsetWidth);
    
    // Clamp the scroll value
    const clampedScroll = Math.max(maxScroll, Math.min(0, newScroll));
    
    track.style.transform = `translateX(${clampedScroll}px)`;
    
    // Update button states
    updateCarouselButtons(carouselId, clampedScroll, maxScroll);
}

function updateCarouselButtons(carouselId, currentScroll, maxScroll) {
    const container = document.getElementById(carouselId)?.closest('.carousel-container');
    if (!container) return;
    
    const prevBtn = container.querySelector('.carousel-btn-prev');
    const nextBtn = container.querySelector('.carousel-btn-next');
    
    if (prevBtn) {
        prevBtn.disabled = currentScroll >= 0;
    }
    if (nextBtn) {
        nextBtn.disabled = currentScroll <= maxScroll;
    }
}

// Multiselect dropdown functionality
var expandedStates = {
    authorCheckboxes: false,
    genreCheckboxes: false,
    seriesCheckboxes: false
};

function showCheckboxes(id) {
    var checkboxes = document.getElementById(id);
    if (!checkboxes) return;
    
    if (!expandedStates[id]) {
        checkboxes.style.display = "block";
        expandedStates[id] = true;
    } else {
        checkboxes.style.display = "none";
        expandedStates[id] = false;
    }
}

// Filter main author dropdown based on selected authors
function updateMainAuthorDropdown() {
    const mainAuthorSelect = document.getElementById('mainAuthorId');
    if (!mainAuthorSelect) return;
    
    const authorCheckboxes = document.querySelectorAll('.author-checkbox');
    const checkedAuthorIds = Array.from(authorCheckboxes)
        .filter(cb => cb.checked)
        .map(cb => cb.getAttribute('data-author-id'));
    
    // Get currently selected main author value
    const currentMainAuthorId = mainAuthorSelect.value;
    
    // Show/hide options based on checked authors
    Array.from(mainAuthorSelect.options).forEach(option => {
        if (option.value === '') {
            // Always show the empty option
            option.style.display = 'block';
        } else {
            const authorId = option.getAttribute('data-author-id');
            if (checkedAuthorIds.includes(authorId)) {
                option.style.display = 'block';
            } else {
                option.style.display = 'none';
                // If the current selection is unchecked, clear it
                if (option.value === currentMainAuthorId) {
                    mainAuthorSelect.value = '';
                }
            }
        }
    });
}

// Initialize carousel buttons on page load
document.addEventListener('DOMContentLoaded', function() {
    const carousels = document.querySelectorAll('.carousel-wrapper');
    carousels.forEach(carousel => {
        const track = carousel.querySelector('.carousel-track');
        const container = carousel.closest('.carousel-container');
        if (!track || !container) return;
        
        const maxScroll = -(track.scrollWidth - carousel.offsetWidth);
        updateCarouselButtons(carousel.id, 0, maxScroll);
    });
    
    // Mobile menu toggle
    const menuToggle = document.getElementById('menuToggle');
    const headerList = document.getElementById('headerList');
    const loginNav = document.getElementById('loginNav');
    
    if (menuToggle) {
        menuToggle.addEventListener('click', function() {
            menuToggle.classList.toggle('active');
            if (headerList) headerList.classList.toggle('active');
            if (loginNav) loginNav.classList.toggle('active');
        });
    }
    
    // Initialize multiselect functionality
    const authorCheckboxes = document.querySelectorAll('.author-checkbox');
    if (authorCheckboxes.length > 0) {
        // Add event listeners to author checkboxes
        authorCheckboxes.forEach(checkbox => {
            checkbox.addEventListener('change', updateMainAuthorDropdown);
        });
        
        // Initial update of main author dropdown
        updateMainAuthorDropdown();
    }
    
    // Close dropdowns when clicking outside
    window.onclick = function(event) {
        if (!event.target.matches('.selectBox') && !event.target.closest('.multiselect')) {
            var dropdowns = document.getElementsByClassName("checkboxes");
            for (var i = 0; i < dropdowns.length; i++) {
                var openDropdown = dropdowns[i];
                if (openDropdown.style.display === "block") {
                    openDropdown.style.display = "none";
                    var id = openDropdown.id;
                    if (expandedStates[id] !== undefined) {
                        expandedStates[id] = false;
                    }
                }
            }
        }
    };
});


