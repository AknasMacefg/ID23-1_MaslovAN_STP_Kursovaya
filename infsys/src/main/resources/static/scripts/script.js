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


