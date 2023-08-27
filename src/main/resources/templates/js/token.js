

function setToken(token) {
    localData.setItem(localStorageTokenKey, 'Bearer ' + token);
}

function getToken() {
    return localData.getItem(localStorageTokenKey);
}
function removeToken() {
    localData.removeItem(localStorageTokenKey);
}
