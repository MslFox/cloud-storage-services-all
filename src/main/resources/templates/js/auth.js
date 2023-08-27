localStorage.getItem(localStorageTokenKey)
document.addEventListener("DOMContentLoaded", function () {
    let openApiLink = document.getElementById("openApiLink");
    let apiDocumentationUrl = serverUrl + "/swagger-ui/index.html#/"; // Replace with your API documentation URL

    openApiLink.setAttribute("href", apiDocumentationUrl);
    openApiLink.addEventListener("click", function (event) {
        event.preventDefault();
        window.open(apiDocumentationUrl, "_blank");
    });
});
let form = document.getElementById("input-form");
window.onload = function () {
    let emailInput = document.getElementById("email");
    let passwordInput = document.getElementById("password");
    emailInput.value = "";
    passwordInput.value = "";
};

function togglePasswordVisibility() {
    const passwordInput = document.getElementById("password");
    if (passwordInput.type === "password") {
        passwordInput.type = "text";
    } else {
        passwordInput.type = "password";
    }
}

window.addEventListener('load', function () {
    document.getElementById('show').checked = false;
    document.getElementById('email').value = '';
    document.getElementById('password').value = '';
});

function validateEmail(email) {
    if (!email.value ||
        !email.value.includes('@') ||
        email.value.indexOf('@') === 0 ||
        email.value.indexOf('@') === email.value.length - 1) {
        displayErrorMessage(email, 'Please enter a valid email address');
        return false;
    }
    return true;
}

function validatePassword(password) {
    const re = /^(?=.*\d)(?=.*[A-Z]).{6,}$/;
    if (!re.test(password.value)) {
        displayErrorMessage(password, 'Password must be at least 6 characters and contain at least one digit and one uppercase letter');
        return false;
    }
    return true;
}

form.addEventListener('submit', function (event) {
    event.preventDefault();
    let email = document.getElementById('email');
    let password = document.getElementById('password');
    if (!(validateEmail(email) &&
        validatePassword(password))) {
        return;
    }
    let data = {
        login: email.value,
        password: password.value
    };
    let xhr = new XMLHttpRequest();
    xhr.open('POST', serverUrl + '/login');
    xhr.setRequestHeader('Content-Type', 'application/json');
    showSpinningIndicator('Connecting...');
    xhr.onreadystatechange = function () {
        if (xhr.readyState === XMLHttpRequest.DONE) {
            let field = document.getElementById('show-password');
            try {
                if (xhr.status === 200) {
                    setToken(JSON.parse(xhr.responseText)['auth-token']);
                    email.value = '';
                    password.value = '';
                    data = '';
                    window.location.href = './home.html';
                } else {
                    displayErrorMessage(field, JSON.parse(xhr.responseText).message);
                }

            } catch (error) {
                displayErrorMessage(field, "Connection Error: Connection Refused");
            }
            hideSpinningIndicator();
        }
    };
    xhr.send(JSON.stringify(data));
})

function displayErrorMessage(field, message) {
    let errorMessage = field.parentNode.querySelector(".error-message");
    if (errorMessage != null) {
        field.parentNode.removeChild(errorMessage);
    }
    errorMessage = document.createElement("div");
    errorMessage.className = "error-message";
    errorMessage.textContent = message;
    field.insertAdjacentElement('afterend', errorMessage);
}
