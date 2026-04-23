// Event form JavaScript
let selectedUsers = new Set();

document.addEventListener('DOMContentLoaded', function() {
    // Handle visibility radio buttons
    document.querySelectorAll('input[name="openToAll"]').forEach(radio => {
        radio.addEventListener('change', function() {
            const inviteSection = document.getElementById('inviteUsersSection');
            if (this.value === 'false') {
                inviteSection.style.display = 'block';
            } else {
                inviteSection.style.display = 'none';
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
