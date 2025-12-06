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
});

