// Calendar JavaScript
let currentDate = new Date();
let currentEvents = [];
let selectedDateForEvent = null;
let currentUserId = null;
let selectedUsers = new Set();

const monthNames = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
];

document.addEventListener('DOMContentLoaded', function() {
    // Get current user info from page
    const userElement = document.querySelector('.navbar-text span');
    if (userElement) {
        // We'll need to pass user ID from backend, for now we'll check during RSVP
    }

    renderCalendar();
    loadEvents();

    document.getElementById('prevMonth').addEventListener('click', function() {
        currentDate.setMonth(currentDate.getMonth() - 1);
        renderCalendar();
        loadEvents();
    });

    document.getElementById('nextMonth').addEventListener('click', function() {
        currentDate.setMonth(currentDate.getMonth() + 1);
        renderCalendar();
        loadEvents();
    });

    document.getElementById('addEventBtn').addEventListener('click', function() {
        const eventModal = bootstrap.Modal.getInstance(document.getElementById('eventModal'));
        eventModal.hide();
        
        const addEventModal = new bootstrap.Modal(document.getElementById('addEventModal'));
        addEventModal.show();
    });

    document.getElementById('saveEventBtn').addEventListener('click', createQuickEvent);

    // Handle visibility radio buttons
    document.querySelectorAll('input[name="openToAll"]').forEach(radio => {
        radio.addEventListener('change', function() {
            const inviteSection = document.getElementById('eventInviteUsersSection');
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
    const searchInput = document.getElementById('eventUserSearchInput');
    const searchResults = document.getElementById('eventUserSearchResults');

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

function renderCalendar() {
    const year = currentDate.getFullYear();
    const month = currentDate.getMonth();

    document.getElementById('monthYear').textContent = `${monthNames[month]} ${year}`;

    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const prevLastDay = new Date(year, month, 0);

    const firstDayOfWeek = firstDay.getDay();
    const lastDateOfMonth = lastDay.getDate();
    const prevLastDate = prevLastDay.getDate();

    const calendarGrid = document.getElementById('calendarGrid');

    // Remove all existing day elements (keep headers)
    const dayElements = calendarGrid.querySelectorAll('.calendar-day');
    dayElements.forEach(el => el.remove());

    // Previous month days
    for (let i = firstDayOfWeek - 1; i >= 0; i--) {
        const dayElement = createDayElement(prevLastDate - i, true, year, month - 1);
        calendarGrid.appendChild(dayElement);
    }

    // Current month days
    const today = new Date();
    for (let day = 1; day <= lastDateOfMonth; day++) {
        const isToday = day === today.getDate() &&
                       month === today.getMonth() &&
                       year === today.getFullYear();
        const dayElement = createDayElement(day, false, year, month, isToday);
        calendarGrid.appendChild(dayElement);
    }

    // Next month days
    const totalDays = firstDayOfWeek + lastDateOfMonth;
    const remainingCells = Math.ceil(totalDays / 7) * 7 - totalDays;
    for (let day = 1; day <= remainingCells; day++) {
        const dayElement = createDayElement(day, true, year, month + 1);
        calendarGrid.appendChild(dayElement);
    }
}

function createDayElement(day, isOtherMonth, year, month, isToday = false) {
    const dayElement = document.createElement('div');
    dayElement.className = 'calendar-day';
    if (isOtherMonth) dayElement.classList.add('other-month');
    if (isToday) dayElement.classList.add('today');

    const dayNumber = document.createElement('div');
    dayNumber.className = 'calendar-day-number';
    dayNumber.textContent = day;
    dayElement.appendChild(dayNumber);

    const eventsContainer = document.createElement('div');
    eventsContainer.className = 'events-container';
    dayElement.appendChild(eventsContainer);

    // Store date info
    dayElement.dataset.year = year;
    dayElement.dataset.month = month;
    dayElement.dataset.day = day;

    dayElement.addEventListener('click', function() {
        showDayEvents(year, month, day);
    });

    return dayElement;
}

function loadEvents() {
    const year = currentDate.getFullYear();
    const month = currentDate.getMonth();

    const startDate = new Date(year, month, 1);
    const endDate = new Date(year, month + 1, 0, 23, 59, 59);

    const startStr = formatDateTimeForAPI(startDate);
    const endStr = formatDateTimeForAPI(endDate);

    console.log('Loading events for:', startStr, 'to', endStr);

    return fetch(`/calendar/events?start=${startStr}&end=${endStr}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to load events');
            }
            return response.json();
        })
        .then(events => {
            console.log('Events loaded:', events.length, 'events');
            currentEvents = events;
            displayEventsOnCalendar();
            return events;
        })
        .catch(error => {
            console.error('Error loading events:', error);
            return [];
        });
}

function displayEventsOnCalendar() {
    console.log('Displaying', currentEvents.length, 'events on calendar');

    // Clear existing events
    document.querySelectorAll('.events-container').forEach(container => {
        container.innerHTML = '';
    });

    if (currentEvents.length === 0) {
        console.log('No events to display');
        return;
    }

    currentEvents.forEach(event => {
        const eventDate = new Date(event.startTime);
        console.log('Processing event:', event.title, 'on', eventDate);

        const dayElements = document.querySelectorAll('.calendar-day');

        dayElements.forEach(dayElement => {
            const elemYear = parseInt(dayElement.dataset.year);
            const elemMonth = parseInt(dayElement.dataset.month);
            const elemDay = parseInt(dayElement.dataset.day);

            if (eventDate.getFullYear() === elemYear &&
                eventDate.getMonth() === elemMonth &&
                eventDate.getDate() === elemDay) {

                console.log('Found matching day for event:', event.title);
                const eventsContainer = dayElement.querySelector('.events-container');
                const existingEvents = eventsContainer.querySelectorAll('.calendar-event').length;

                if (existingEvents < 3) {
                    const eventElement = document.createElement('div');
                    eventElement.className = 'calendar-event';
                    eventElement.innerHTML = `
                        <span class="calendar-event-time">${formatTime(eventDate)}</span>
                        ${event.title}
                    `;
                    eventElement.addEventListener('click', function(e) {
                        e.stopPropagation();
                        showEventDetails(event);
                    });
                    eventsContainer.appendChild(eventElement);
                } else if (existingEvents === 3) {
                    const moreCount = getEventsCountForDay(elemYear, elemMonth, elemDay) - 3;
                    if (moreCount > 0) {
                        const badge = document.createElement('div');
                        badge.className = 'event-count-badge';
                        badge.textContent = `+${moreCount} more`;
                        eventsContainer.appendChild(badge);
                    }
                }
            }
        });
    });
}

function getEventsCountForDay(year, month, day) {
    return currentEvents.filter(event => {
        const eventDate = new Date(event.startTime);
        return eventDate.getFullYear() === year &&
               eventDate.getMonth() === month &&
               eventDate.getDate() === day;
    }).length;
}

function showDayEvents(year, month, day) {
    const dayEvents = currentEvents.filter(event => {
        const eventDate = new Date(event.startTime);
        return eventDate.getFullYear() === year &&
               eventDate.getMonth() === month &&
               eventDate.getDate() === day;
    });

    selectedDateForEvent = new Date(year, month, day);
    const dateStr = selectedDateForEvent.toLocaleDateString('en-US', { 
        weekday: 'long', 
        year: 'numeric', 
        month: 'long', 
        day: 'numeric' 
    });

    document.getElementById('eventModalTitle').textContent = `Events for ${dateStr}`;
    const modalBody = document.getElementById('eventModalBody');

    if (dayEvents.length === 0) {
        modalBody.innerHTML = '<p class="text-muted">No events for this day. Click "Add Event" to create one.</p>';
    } else {
        modalBody.innerHTML = dayEvents.map(event => createEventDetailHTML(event)).join('');
        
        // Add RSVP listeners
        modalBody.querySelectorAll('.rsvp-btn').forEach(btn => {
            btn.addEventListener('click', function() {
                const eventId = this.dataset.eventId;
                const action = this.dataset.action;
                handleRSVP(eventId, action);
            });
        });
    }

    const eventModal = new bootstrap.Modal(document.getElementById('eventModal'));
    eventModal.show();
}

function createEventDetailHTML(event) {
    const startTime = new Date(event.startTime);
    const endTime = new Date(event.endTime);
    
    // Determine RSVP status
    let rsvpHTML = '';
    const currentUsername = document.querySelector('.navbar-text span').textContent.trim();
    
    if (event.creatorUsername === currentUsername) {
        rsvpHTML = '<span class="rsvp-status creator">You created this</span>';
    } else if (event.participantUsernames && event.participantUsernames.includes(currentUsername)) {
        rsvpHTML = `
            <span class="rsvp-status joined">You're attending</span>
            <button class="btn btn-sm btn-warning rsvp-btn" data-event-id="${event.id}" data-action="leave">
                Leave Event
            </button>
        `;
    } else {
        rsvpHTML = `
            <button class="btn btn-sm btn-success rsvp-btn" data-event-id="${event.id}" data-action="join">
                RSVP / Join Event
            </button>
        `;
    }

    return `
        <div class="event-details">
            <h5>${event.title}</h5>
            <p><strong>Time:</strong> ${formatTime(startTime)} - ${formatTime(endTime)}</p>
            <p><strong>Description:</strong> ${event.description}</p>
            ${event.location ? `<p><strong>Location:</strong> ${event.location}</p>` : ''}
            <p><strong>Created by:</strong> ${event.creatorUsername}</p>
            <p><strong>Participants:</strong> ${event.participantCount}</p>
            <div class="event-actions">
                ${rsvpHTML}
                <a href="/events/${event.id}" class="btn btn-sm btn-primary">View Full Details</a>
            </div>
        </div>
    `;
}

function handleRSVP(eventId, action) {
    const url = `/events/${eventId}/${action}`;

    // Get CSRF token from meta tag
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    const headers = {
        'Content-Type': 'application/x-www-form-urlencoded',
    };
    if (csrfToken && csrfHeader) {
        headers[csrfHeader] = csrfToken;
    }

    fetch(url, {
        method: 'POST',
        headers: headers,
    })
    .then(response => {
        if (response.ok) {
            // Reload events and update display
            loadEvents();
            // Close and reopen modal to refresh
            const eventModal = bootstrap.Modal.getInstance(document.getElementById('eventModal'));
            if (eventModal) {
                eventModal.hide();
            }
            setTimeout(() => {
                if (selectedDateForEvent) {
                    showDayEvents(
                        selectedDateForEvent.getFullYear(),
                        selectedDateForEvent.getMonth(),
                        selectedDateForEvent.getDate()
                    );
                }
            }, 300);
        }
    })
    .catch(error => console.error('Error with RSVP:', error));
}

function showEventDetails(event) {
    const modalBody = document.getElementById('eventModalBody');
    modalBody.innerHTML = createEventDetailHTML(event);
    
    // Add RSVP listeners
    modalBody.querySelectorAll('.rsvp-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            const eventId = this.dataset.eventId;
            const action = this.dataset.action;
            handleRSVP(eventId, action);
        });
    });

    const eventModal = new bootstrap.Modal(document.getElementById('eventModal'));
    eventModal.show();
}

function addSelectedUser(userId, username) {
    selectedUsers.add(userId);
    updateSelectedUsersDisplay();
}

function removeSelectedUser(userId) {
    selectedUsers.delete(userId);
    updateSelectedUsersDisplay();
}

function updateSelectedUsersDisplay() {
    const container = document.getElementById('eventSelectedUsersContainer');

    if (selectedUsers.size === 0) {
        container.innerHTML = '<small class="text-muted">No users selected</small>';
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
}

function createQuickEvent() {
    const title = document.getElementById('eventTitle').value;
    const description = document.getElementById('eventDescription').value;
    const startTime = document.getElementById('eventStartTime').value;
    const endTime = document.getElementById('eventEndTime').value;
    const location = document.getElementById('eventLocation').value;
    const openToAll = document.querySelector('input[name="openToAll"]:checked').value === 'true';

    if (!title || !description || !startTime || !endTime) {
        alert('Please fill in all required fields');
        return;
    }

    const startDateTime = new Date(selectedDateForEvent);
    const [startHour, startMinute] = startTime.split(':');
    startDateTime.setHours(parseInt(startHour), parseInt(startMinute), 0);

    const endDateTime = new Date(selectedDateForEvent);
    const [endHour, endMinute] = endTime.split(':');
    endDateTime.setHours(parseInt(endHour), parseInt(endMinute), 0);

    const formData = new URLSearchParams();
    formData.append('title', title);
    formData.append('description', description);
    formData.append('startTime', formatDateTimeForAPI(startDateTime));
    formData.append('endTime', formatDateTimeForAPI(endDateTime));
    if (location) formData.append('location', location);
    formData.append('openToAll', openToAll);

    // Add invited users if not open to all
    if (!openToAll) {
        selectedUsers.forEach(userId => {
            formData.append('invitedUserIds', userId);
        });
    }

    // Get CSRF token from meta tag
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    const headers = {
        'Content-Type': 'application/x-www-form-urlencoded',
    };
    if (csrfToken && csrfHeader) {
        headers[csrfHeader] = csrfToken;
    }

    fetch('/calendar/events/quick-create', {
        method: 'POST',
        headers: headers,
        body: formData
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to create event');
        }
        return response.json();
    })
    .then(event => {
        console.log('Event created:', event);

        // Close modal
        const addEventModal = bootstrap.Modal.getInstance(document.getElementById('addEventModal'));
        if (addEventModal) {
            addEventModal.hide();
        }
        document.getElementById('quickEventForm').reset();

        // Reset visibility settings
        document.getElementById('eventOpenToAllYes').checked = true;
        document.getElementById('eventInviteUsersSection').style.display = 'none';
        selectedUsers.clear();
        updateSelectedUsersDisplay();

        // Reload events and then display them
        loadEvents();
    })
    .catch(error => {
        console.error('Error creating event:', error);
        alert('Failed to create event. Please try again.');
    });
}

function formatDateTimeForAPI(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;
}

function formatTime(date) {
    return date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', hour12: true });
}
