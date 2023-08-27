listFile();
let upload_button = document.getElementById('upload-button');
upload_button.addEventListener('click', function () {
    uploadFile();
});

let exit_button = document.getElementById('exit-button');
exit_button.addEventListener('click', function () {
    logout();
});
let used_memory = document.getElementById('used-memory');

function listFile() {
    let xhr = new XMLHttpRequest();
    xhr.open('GET', serverUrl + '/list?limit=255');
    xhr.setRequestHeader('auth-token', getToken());
    xhr.onreadystatechange = function () {
        if (xhr.readyState === XMLHttpRequest.DONE) {
            if (xhr.status === 200) {
                let list = JSON.parse(xhr.responseText);
                let table = document.getElementById('files-table');
                // if (table) {
                while (table.lastElementChild) {
                    table.removeChild(table.lastElementChild);
                }
                let tableBody = document.createElement('tbody');
                let headerRow = document.createElement('tr');
                // FILENAME
                let filenameHeader = document.createElement('th');
                filenameHeader.className = 'filename_header';
                filenameHeader.textContent = 'Filename';
                // SIZE
                let sizeHeader = document.createElement('th');
                sizeHeader.className = 'size';
                sizeHeader.textContent = 'Size';
                // DATE
                let dateHeader = document.createElement('th');
                dateHeader.className = 'date';
                dateHeader.textContent = 'Date';
                // ICONS
                let iconHeader = document.createElement('th');
                iconHeader.className = 'icons';
                iconHeader.textContent = 'Edit';

                headerRow.appendChild(filenameHeader);
                headerRow.appendChild(sizeHeader);
                headerRow.appendChild(dateHeader);
                headerRow.appendChild(iconHeader);
                tableBody.appendChild(headerRow);
                let current_used_memory = 0;
                for (let i = 0; i < list.length; i++) {
                    let rowData = list[i];
                    let row = document.createElement('tr');
                    // FILENAME
                    let filenameCell = document.createElement('td');
                    filenameCell.className = 'filename';
                    filenameCell.id = 'filename' + i;
                    let filename = rowData.filename;
                    // это работает
                    let filenameSpan = document.createElement('span');
                    filenameSpan.textContent = filename;
                    let filenameInput = document.createElement('input');
                    filenameInput.type = 'text';
                    filenameInput.id = 'input-form'
                    filenameInput.value = filename;

                    filenameInput.style.display = 'none'; // По умолчанию скрываем поле ввода
                    filenameCell.appendChild(filenameSpan);
                    filenameCell.appendChild(filenameInput);
                    filenameSpan.addEventListener('click', function () {
                        downLoadFile(this.textContent);
                    });
                    // SIZE
                    current_used_memory = current_used_memory + rowData.size;
                    let sizeCell = document.createElement('td');
                    sizeCell.className = 'size';
                    if (rowData.size >= 1024 * 1024) {
                        sizeCell.textContent = (rowData.size / (1024 * 1024)).toFixed(1) + 'MB';
                    } else if (rowData.size >= 1024) {
                        sizeCell.textContent = Math.floor(rowData.size / 1024) + 'KB';
                    } else {
                        sizeCell.textContent = rowData.size + 'B';
                    }
                    // DATE
                    let dateCell = document.createElement('td');
                    dateCell.className = 'date';
                    let originalDate = new Date(rowData.date);
                    let formattedDate = originalDate.toLocaleDateString('ru-RU', {
                        day: '2-digit',
                        month: '2-digit',
                        year: '2-digit'
                    }).replace(/\./g, '-');
                    dateCell.textContent = formattedDate;
                    // ICONS
                    let iconsCell = document.createElement('td');
                    iconsCell.className = 'icons';
                    let rename_icon = document.createElement('img');
                    rename_icon.src = './images/icons/edit.svg';
                    rename_icon.id = 'rename-icon';
                    rename_icon.className = 'invertible';
                    rename_icon.setAttribute('title', 'Rename file')
                    rename_icon.addEventListener('click', function () {
                        renameFile(rowData.filename, filenameSpan, filenameInput, filenameCell);
                    });
                    iconsCell.appendChild(rename_icon);
                    let delete_icon = document.createElement('img');
                    delete_icon.src = './images/icons/delete.svg';
                    delete_icon.id = 'delete-icon';
                    delete_icon.className = 'invertible';
                    delete_icon.setAttribute('title', 'Delete file')
                    delete_icon.addEventListener('click', function () {
                        deleteFile(rowData.filename);
                    });
                    iconsCell.appendChild(delete_icon);
                    row.appendChild(filenameCell);
                    row.appendChild(sizeCell);
                    row.appendChild(dateCell);
                    row.appendChild(iconsCell);
                    tableBody.appendChild(row);
                }
                table.appendChild(tableBody);
                used_memory.textContent = 'Used: ' + current_used_memory + ' Bytes';
                used_memory.insertAdjacentElement('afterend', table);
            } else {
                removeToken();
                window.location.href = "./auth.html";
            }
        }
    };
    xhr.send();
}

function deleteFile(filename) {
    confirmWindow(performFileDelete, filename, 'Are you sure you want to delete ' + filename + '?');
}

function performFileDelete(filename) {
    let xhr = new XMLHttpRequest();
    xhr.open('DELETE', serverUrl + '/file?filename=' + filename);
    xhr.setRequestHeader('auth-token', getToken());
    xhr.onreadystatechange = function () {
        if (xhr.readyState === XMLHttpRequest.DONE) {
            if (xhr.status === 200) {
                listFile();
                console.log('Файл успешно удален.');
            } else {
                console.error('Ошибка удаления файла.');
            }
            if (xhr.status === 401) {
                removeToken();
                window.location.href = "./auth.html";
            }
        }
    };
    xhr.send();
}

function renameFile(filename, filenameSpan, filenameInput, filenameCell) {
    filenameSpan.style.display = 'none';
    filenameInput.style.display = 'inline';
    filenameCell.style.zIndex = '1000';
    overlay.style.display = 'block';
    filenameInput.focus();
    filenameInput.addEventListener('keydown', function (event) {
        if (event.keyCode === 13) {
            let newFilename = filenameInput.value;
            if (newFilename !== filename) {
                let newFileData = {filename: newFilename};
                let xhr = new XMLHttpRequest();
                xhr.open('PUT', serverUrl + '/file?filename=' + filename);
                xhr.setRequestHeader('auth-token', getToken());
                xhr.setRequestHeader('Content-Type', 'application/json');

                xhr.onreadystatechange = function () {
                    if (xhr.readyState === XMLHttpRequest.DONE) {
                        if (xhr.status === 200) {
                            listFile();
                            console.log('Файл успешно переименован.');
                        } else {
                            console.error('Ошибка перименования файла.')
                        }
                        if (xhr.status === 401) {
                            removeToken();
                            window.location.href = "./auth.html";
                        }
                    }
                }
                xhr.send(JSON.stringify(newFileData));
            }
            closeInputFilenameForm();
        }
    });

    function closeInputFilenameForm() {
        filenameSpan.style.display = 'inline';
        filenameInput.style.display = 'none';
        overlay.style.display = 'none';
        closeDialogWindow();
    }
}

document.addEventListener('keydown', function (event) {
    if (event.key === 'Escape') {
        closeDialogWindow();
    }
})
function downLoadFile(filename) {
    confirmWindow(performDownLoadFile, filename, 'Are you sure you want to download ' + filename + '?');
}
function performDownLoadFile(filename) {
    let downloadLink = document.createElement('a');

    fetch(serverUrl + '/file?filename=' + filename, {
        headers: {'auth-token': getToken()}
    })
        .then(response => {
            if (!response.ok) {
                throw new Error("Ошибка загрузки файла");
            }
            return response.text();
        }).then(linkKey => {
        console.log('then click' + downloadLink);
        downloadLink.href = serverUrl + '/download/' + linkKey;
        downloadLink.download = filename;
        downloadLink.style.display = 'none';
        document.body.appendChild(downloadLink);
        downloadLink.click();
        document.body.removeChild(downloadLink);
    })
        .catch(error => console.error('Ошибка получения ссылки ', error));
}

function uploadFile() {
    const fileInput = document.createElement('input');
    fileInput.type = 'file';
    fileInput.addEventListener('change', function () {
        const file = event.target.files[0]; // Получаем выбранный файл
        const formData = new FormData();
        formData.append('filename', file.name);
        formData.append('file', file);
        const xhr = new XMLHttpRequest();
        xhr.open('POST', serverUrl + '/file');
        xhr.setRequestHeader('auth-token', getToken());
        showSpinningIndicator('Loading...');
        xhr.onload = function () {
            if (xhr.status === 200) {
                listFile();
            } else {
                throw new Error("Ошибка загрузки файла");
            }
            hideSpinningIndicator();
        };
        xhr.send(formData);
    });
    fileInput.click();
}

function logout() {
    confirmWindow(performLogout, null, 'Are you sure you want to exit?');
}

async function performLogout() {
    try {
        const response = await fetch(serverUrl + '/logout', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'auth-token': getToken()
            }
        });
        if (response.status === 200) {
            window.location.href = './auth.html';
        } else {
            throw new Error('Ошибка выхода');
        }
    } catch (error) {
        console.error(error.message);
    }
    removeToken();
}


