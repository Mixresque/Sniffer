(function () {

    // variables
    var user_id = 'ddhee';
    var user_fullname = '';
    var lat = 40.73;
    var lng = -73.99;

    function init() {
        // Register event listeners
        $('login-btn').addEventListener('click', login);
        $('nearby-btn').addEventListener('click', loadNearbyRestaurants);
        $('fav-btn').addEventListener('click', loadFavoriteRestaurants);
        $('recommend-btn').addEventListener('click', loadRecommendedRestaurants);

        // validateSession();

        onSessionValid({
            user_id: 'ddhee',
            name: 'Dd Hee'
        });
    }

    // -------------
    //   Session
    // -------------
    function validateSession() {
        // The request parameters
        let url = './LoginServlet';
        let req = JSON.stringify({});

        // display loading message
        showLoadingMessage('Validating session...');

        // make AJAX call
        ajax('GET', url, req,
            // session is still valid
            function (res) {
                let result = JSON.parse(res);

                if (result.status === 'OK') {
                    onSessionValid(result);
                }
            }
        );
    }

    function onSessionValid(result) {
        user_id = result.user_id;
        user_fullname = result.name;

        let loginForm = $('login-form');
        let restaurantNav = $('restaurant-nav');
        let restaurantList = $('restaurant-list');
        let avatar = $('avatar');
        let welcomeMsg = $('welcome-msg');
        let logoutBtn = $('logout-link');

        welcomeMsg.innerHTML = 'Welcome, ' + user_fullname;

        showElement(restaurantNav);
        showElement(restaurantList);
        showElement(avatar);
        showElement(welcomeMsg);
        showElement(logoutBtn, 'inline-block');
        hideElement(loginForm);

        initGeoLocation();
    }

    function onSessionInvalid() {
        let loginForm = $('login-form');
        let restaurantNav = $('restaurant-nav');
        let restaurantList = $('restaurant-list');
        let avatar = $('avatar');
        let welcomeMsg = $('welcome-msg');
        let logoutBtn = $('logout-link');

        hideElement(restaurantNav);
        hideElement(restaurantList);
        hideElement(avatar);
        hideElement(logoutBtn);
        hideElement(welcomeMsg);

        showElement(loginForm);
    }

    function initGeoLocation() {
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(onPositionUpdated, onLoadPositionFailed, {maximumAge: 60000});
            showLoadingMessage('Retrieving your location...');
        } else {
            onLoadPositionFailed();
        }
    }

    function onPositionUpdated(position) {
        lat = position.coords.latitude;
        lng = position.coords.longitude;

        loadNearbyRestaurants();
    }

    function onLoadPositionFailed() {
        console.warn('navigator.geolocation is not available');
        //loadNearbyRestaurants();
        getLocationFromIP();
    }

    function getLocationFromIP() {
        // Get location from http://ipinfo.io/json
        let url = 'http://ipinfo.io/json'
        let req = null;
        ajax('GET', url, req,
            // session is still valid
            function (res) {
                let result = JSON.parse(res);
                if ('loc' in result) {
                    let loc = result.loc.split(',');
                    lat = loc[0];
                    lng = loc[1];
                } else {
                    console.warn('Getting location by IP failed.');
                }
                loadNearbyRestaurants();
            }
        );
    }

    //--------------------
    //  Login
    //--------------------

    function login() {
        let username = $('username').value;
        let password = $('password').value;
        password = md5(username + md5(password));

        //The request parameters
        let url = './LoginServlet';
        let params = 'user_id=' + username + '&password=' + password;
        let req = JSON.stringify({});

        ajax('POST', url + '?' + params, req,
            // successful callback
            function (res) {
                let result = JSON.parse(res);

                // successfully logged in
                if (result.status === 'OK') {
                    onSessionValid(result);
                }
            },
            // error
            function () {
                showLoginError();
            }
        );
    }

    function showLoginError() {
        $('login-error').innerHTML = 'Invalid username or password';
    }

    function clearLoginError() {
        $('login-error').innerHTML = '';
    }

    // -------------------------------------
    //  Create restaurant list
    // -------------------------------------

    /**
     * List restaurants
     *
     * @param restaurants - An array of restaurant JSON objects
     */
    function listRestaurants(restaurants) {
        // Clear the current results
        let restaurantList = $('restaurant-list');
        restaurantList.innerHTML = '';

        for (let i = 0; i < restaurants.length; i++) {
            addRestaurant(restaurantList, restaurants[i]);
        }
    }

    /**
     * Add restaurant to the list
     *
     * @param restaurantList - The <ul id="restaurant-list"> tag
     * @param restaurant - The restaurant data (JSON object)
     */
    function addRestaurant(restaurantList, restaurant) {
        let business_id = restaurant.business_id;

        // create the <li> tag and specify the id and class attributes
        let li = $('li', {
            id: 'restaurant-' + business_id,
            className: 'restaurant'
        });

        // set the data attribute
        li.dataset.business = business_id;
        li.dataset.visited = restaurant.visited;

        // restaurant image
        li.appendChild($('img', {src: restaurant.image_url}));

        // section
        let section = $('div', {});

        // title
        let title = $('a', {href: restaurant.url, target: '_blank', className: 'restaurant-name'});
        title.innerHTML = restaurant.name;
        section.appendChild(title);

        // category
        let category = $('p', {className: 'restaurant-category'});
        category.innerHTML = restaurant.categories.join(', ');
        section.appendChild(category);

        // stars
        let stars = $('div', {className: 'stars'});
        for (let i = 0; i < Math.floor(restaurant.stars); i++) {
            let star = $('i', {className: 'fa fa-star'});
            stars.appendChild(star);
        }

        if (('' + restaurant.stars).match(/\.5$/)) {
            stars.appendChild($('i', {className: 'fa fa-star-half-o'}));
        }

        section.appendChild(stars);

        li.appendChild(section);

        // address
        let address = $('p', {className: 'restaurant-address'});

        address.innerHTML = restaurant.address.replace(/,/g, '<br/>');
        li.appendChild(address);

        // favorite link
        let favLink = $('p', {className: 'fav-link'});

        favLink.onclick = function () {
            changeFavoriteRestaurant(business_id);
        };

        favLink.appendChild($('i', {
            id: 'fav-icon-' + business_id,
            className: restaurant.visited ? 'fa fa-heart' : 'fa fa-heart-o'
        }));

        li.appendChild(favLink);

        restaurantList.appendChild(li);
    }

    // --------------------
    //   Helper functions
    // --------------------

    /**
     * A helper function that activate a navigation button
     * and deactivates others
     *
     * @param btnId - The id of the navigation button
     */
    function activeBtn(btnId) {
        let btns = document.getElementsByClassName('main-nav-btn');

        // deactivate all navigation buttons
        for (let i = 0; i < btns.length; i++) {
            // btns[i].classList.remove("active");  // IE 9 doesn't support classList
            btns[i].className = btns[i].className.replace(/\bactive\b/, '');
        }

        // active the one that has id = btnId
        let btn = $(btnId);
        btn.className += ' active';
    }

    // show loading
    function showLoadingMessage(msg) {
        let restaurantList = $('restaurant-list');
        restaurantList.innerHTML = '<p class="notice"><i class="fa fa-spinner fa-spin"></i> ' + msg + '</p>';
    }

    // show warning
    function showWarningMessage(msg) {
        let restaurantList = $('restaurant-list');
        restaurantList.innerHTML = '<p class="notice"><i class="fa fa-exclamation-triangle"></i> ' + msg + '</p>';
    }

    // show error
    function showErrorMessage(msg) {
        let restaurantList = $('restaurant-list');
        restaurantList.innerHTML = '<p class="notice"><i class="fa fa-exclamation-circle"></i> ' + msg + '</p>';
    }

    /**
     * A helper function that creates a DOM element <tag options...>
     *
     * @param tag
     * @param options
     * @returns
     */
    function $(tag, options) {
        if (!options) {
            return document.getElementById(tag);
        }

        let element = document.createElement(tag);

        for (let option in options) {
            if (options.hasOwnProperty(option)) {
                element[option] = options[option];
            }
        }

        return element;
    }

    // Hide an element
    function hideElement(element) {
        element.style.display = 'none';
    }

    // Show an element, default style as block
    function showElement(element, style) {
        let displayStyle = style ? style : 'block';
        element.style.display = displayStyle;
    }

    /**
     * AJAX helper
     *
     * @param method - GET|POST|PUT|DELETE
     * @param url - API end point
     * @param data - Data to send if any
     * @param callback - This the successful callback
     * @param errorHandler - This is the failed callback
     */
    function ajax(method, url, data, callback, errorHandler) {
        let xhr = new XMLHttpRequest();

        xhr.open(method, url, true);

        xhr.onload = function () {
            switch (xhr.status) {
                case 200:
                    callback(xhr.responseText);
                    break;
                case 403:
                    onSessionInvalid();
                    break;
                case 401:
                    errorHandler();
                    break;
            }
        };

        xhr.onerror = function () {
            console.error("The request couldn't be completed.");
            errorHandler();
        };

        if (data === null) {
            xhr.send();
        } else {
            xhr.setRequestHeader("Content-Type", "application/json;charset=utf-8");
            xhr.send(data);
        }
    }

// -------------------------------------
//  AJAX call server-side APIs
// -------------------------------------

    /**
     * API #1
     * Load the nearby restaurants
     * API end point: [GET] /Sniffer/restaurants?user_id=1111&lat=37.38&lon=-122.08
     */
    function loadNearbyRestaurants() {
        activeBtn('nearby-btn');

        // The request parameters
        let url = './restaurants';
        let params = 'user_id=' + user_id + '&lat=' + lat + '&lon=' + lng;
        let req = JSON.stringify({});

        // display loading message
        showLoadingMessage('Loading nearby restaurants...');

        // make AJAX call
        ajax('GET', url + '?' + params, req,
            // successful callback
            function (res) {
                let restaurants = JSON.parse(res);
                if (!restaurants || restaurants.length === 0) {
                    showWarningMessage('No nearby restaurant.');
                } else {
                    listRestaurants(restaurants);
                }
            },
            // failed callback
            function () {
                showErrorMessage('Cannot load nearby restaurants.');
            }
        );
    }

    /**
     * API #2
     * Load favorite (or visited) restaurants
     * API end point: [GET] /Sniffer/history?user_id=1111
     */
    function loadFavoriteRestaurants(event) {
        event.preventDefault();
        activeBtn('fav-btn');

        // The request parameters
        let url = './history';
        let params = 'user_id=' + user_id;
        let req = JSON.stringify({});

        // display loading message
        showLoadingMessage('Loading favorite restaurants...');

        // make AJAX call
        ajax('GET', url + '?' + params, req,
            function (res) {
                let restaurants = JSON.parse(res);
                if (!restaurants || restaurants.length === 0) {
                    showWarningMessage('No favorite restaurant.');
                } else {
                    listRestaurants(restaurants);
                }
            },
            function () {
                showErrorMessage('Cannot load favorite restaurants.');
            }
        );
    }

    /**
     * API #3
     * Load recommended restaurants
     * API end point: [GET] /Sniffer/recommendation?user_id=1111
     */
    function loadRecommendedRestaurants() {
        activeBtn('recommend-btn');

        // The request parameters
        let url = './recommendation';
        let params = 'user_id=' + user_id;
        let req = JSON.stringify({});

        // display loading message
        showLoadingMessage('Loading recommended restaurants...');

        // make AJAX call
        ajax('GET', url + '?' + params, req,
            // successful callback
            function (res) {
                let restaurants = JSON.parse(res);
                if (!restaurants || restaurants.length === 0) {
                    showWarningMessage('No recommended restaurant. Make sure you have favorites.');
                } else {
                    listRestaurants(restaurants);
                }
            },
            // failed callback
            function () {
                showErrorMessage('Cannot load recommended restaurants.');
            }
        );
    }

    /**
     * API #4
     * Toggle favorite (or visited) restaurants
     *
     * @param business_id - The restaurant business id
     *
     * API end point: [POST]/[DELETE] /Sniffer/history
     * request json data: { user_id: 1111, visited: [a_list_of_business_ids] }
     */
    function changeFavoriteRestaurant(business_id) {
        // Check whether this restaurant has been visited or not
        let li = $('restaurant-' + business_id);
        let favIcon = $('fav-icon-' + business_id);
        let visited = li.dataset.visited !== 'true';

        // The request parameters
        let url = './history';
        let req = JSON.stringify({
            user_id: user_id,
            visited: [business_id]
        });
        let method = visited ? 'POST' : 'DELETE';

        ajax(method, url, req,
            // successful callback
            function (res) {
                li.dataset.visited = visited;
                favIcon.className = visited ? 'fa fa-heart' : 'fa fa-heart-o';
            }
        );
    }

    init();
})();
