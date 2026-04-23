// Survey form JavaScript
let timeSlotCount = 0;
let selectedUsers = new Set(); // Store selected user objects

document.addEventListener('DOMContentLoaded', function() {
    // Add initial time slot
    addTimeSlotInput();

    document.getElementById('addTimeSlot').addEventListener('click', addTimeSlotInput);

    // Handle visibility radio buttons
    document.querySelectorAll('input[name="openToAll"]').forEach(radio => {
        radio.addEventListener('change', function() {
            const inviteSection = document.getElementById('inviteUsersSection');
            if (this.value === 'false') {
                inviteSection.style.display = 'block';
            } else {
                inviteSection.style.display = 'none';
                // Clear selected users when switching to "Open to All"
                selectedUsers.clear();
                updateSelectedUsersDisplay();
            }
        });
    });

    // Handle user search input
    const searchInput = document.getElementById('userSearchInput');
    const searchResults = document.getElementById('userSearchResults');

    searchInput.addEventListener('input', function() {
        const query = this.value.toLowerCase().trim();

        if (query.length === 0) {
            searchResults.style.display = 'none';
            return;
        }

        // Filter users that match the query and aren't already selected
        const filteredUsers = allUsers.filter(user =>
            user.username.toLowerCase().includes(query) &&
            !selectedUsers.has(user.id)
        );

        if (filteredUsers.length === 0) {
            searchResults.style.display = 'none';
            return;
        }

        // Display results
        searchResults.innerHTML = filteredUsers.map(user => `
            <button type="button" class="list-group-item list-group-item-action" data-user-id="${user.id}" data-username="${user.username}">
                ${user.username}
            </button>
        `).join('');

        searchResults.style.display = 'block';

        // Add click handlers to results
        searchResults.querySelectorAll('button').forEach(btn => {
            btn.addEventListener('click', function() {
                const userId = parseInt(this.dataset.userId);
                const username = this.dataset.username;
                addSelectedUser(userId, username);
                searchInput.value = '';
                searchResults.style.display = 'none';
            });
        });
    });

    // Close search results when clicking outside
    document.addEventListener('click', function(e) {
        if (!searchInput.contains(e.target) && !searchResults.contains(e.target)) {
            searchResults.style.display = 'none';
        }
    });

    // Handle global end time toggle
    document.getElementById('globalHasEndTime').addEventListener('change', function() {
        const hasEndTime = this.checked;

        // Update all existing time slots
        for (let i = 1; i <= timeSlotCount; i++) {
            const hiddenInput = document.getElementById(`hasEndTime${i}`);
            const endTimeContainer = document.getElementById(`endTimeContainer${i}`);
            const endTimeInput = document.getElementById(`endTime${i}`);

            if (hiddenInput && endTimeContainer && endTimeInput) {
                hiddenInput.value = hasEndTime ? 'true' : 'false';
                endTimeContainer.style.display = hasEndTime ? 'block' : 'none';

                if (!hasEndTime) {
                    endTimeInput.value = '';
                    endTimeInput.setCustomValidity('');
                }
            }
        }
    });
});

function addSelectedUser(userId, username) {
    selectedUsers.add(userId);
    updateSelectedUsersDisplay();
}

function removeSelectedUser(userId) {
    selectedUsers.delete(userId);
    updateSelectedUsersDisplay();
}

function updateSelectedUsersDisplay() {
    const container = document.getElementById('selectedUsersContainer');
    const inputsContainer = document.getElementById('invitedUsersInputs');

    if (selectedUsers.size === 0) {
        container.innerHTML = '<small class="text-muted">No users selected</small>';
        inputsContainer.innerHTML = '';
        return;
    }

    // Display badges
    const badges = Array.from(selectedUsers).map(userId => {
        const user = allUsers.find(u => u.id === userId);
        return `
            <span class="badge bg-secondary me-1 mb-1" style="font-size: 0.9rem;">
                ${user.username}
                <button type="button" class="btn-close btn-close-white ms-1" style="font-size: 0.6rem;" onclick="removeSelectedUser(${userId})"></button>
            </span>
        `;
    }).join('');
    container.innerHTML = badges;

    // Add hidden inputs for form submission
    const inputs = Array.from(selectedUsers).map(userId =>
        `<input type="hidden" name="invitedUserIds" value="${userId}">`
    ).join('');
    inputsContainer.innerHTML = inputs;
}

function addTimeSlotInput() {
    timeSlotCount++;
    const container = document.getElementById('timeSlotsContainer');
    const globalHasEndTime = document.getElementById('globalHasEndTime').checked;

    const timeSlotDiv = document.createElement('div');
    timeSlotDiv.className = 'time-slot-input-group';
    timeSlotDiv.id = `timeSlot${timeSlotCount}`;

    timeSlotDiv.innerHTML = `
        ${timeSlotCount > 1 ? `<button type="button" class="btn btn-sm btn-danger remove-slot-btn" onclick="removeTimeSlot(${timeSlotCount})">
            <i class="bi bi-x"></i>
        </button>` : ''}
        <h6 class="mb-3" style="color: var(--primary-color)">Time Slot ${timeSlotCount}</h6>
        <input type="hidden" id="hasEndTime${timeSlotCount}" name="hasEndTimes" value="${globalHasEndTime ? 'true' : 'false'}">
        <div class="row">
            <div class="col-md-6 mb-3">
                <label for="startTime${timeSlotCount}" class="form-label">Start Time</label>
                <input type="datetime-local" class="form-control" id="startTime${timeSlotCount}" name="startTimes" required>
            </div>
            <div class="col-md-6 mb-3" id="endTimeContainer${timeSlotCount}" style="display: ${globalHasEndTime ? 'block' : 'none'}">
                <label for="endTime${timeSlotCount}" class="form-label">End Time</label>
                <input type="datetime-local" class="form-control" id="endTime${timeSlotCount}" name="endTimes" ${globalHasEndTime ? '' : ''}>
            </div>
        </div>
    `;

    container.appendChild(timeSlotDiv);

    // Set minimum datetime to now
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    const minDateTime = `${year}-${month}-${day}T${hours}:${minutes}`;

    document.getElementById(`startTime${timeSlotCount}`).min = minDateTime;
    if (globalHasEndTime) {
        document.getElementById(`endTime${timeSlotCount}`).min = minDateTime;
    }

    // Add validation
    const startInput = document.getElementById(`startTime${timeSlotCount}`);
    const endInput = document.getElementById(`endTime${timeSlotCount}`);

    function validateTimes() {
        const hasEndTime = document.getElementById(`hasEndTime${timeSlotCount}`).value === 'true';
        if (hasEndTime && startInput.value && endInput.value) {
            const startTime = new Date(startInput.value);
            const endTime = new Date(endInput.value);

            if (endTime <= startTime) {
                endInput.setCustomValidity('End time must be after start time');
            } else {
                endInput.setCustomValidity('');
            }
        } else {
            endInput.setCustomValidity('');
        }
    }

    startInput.addEventListener('change', validateTimes);
    endInput.addEventListener('change', validateTimes);
}

function removeTimeSlot(id) {
    const element = document.getElementById(`timeSlot${id}`);
    if (element) {
        element.remove();
    }
}

// Form validation
document.getElementById('surveyForm').addEventListener('submit', function(e) {
    const timeSlots = document.querySelectorAll('.time-slot-input-group');
    if (timeSlots.length === 0) {
        e.preventDefault();
        alert('Please add at least one time slot');
        return false;
    }
});
