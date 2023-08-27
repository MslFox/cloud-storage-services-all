const overlay = document.createElement('div');
overlay.id='overlay';
document.body.appendChild(overlay);


const confirmationDialog = document.createElement('div');
confirmationDialog.id='confirmation-dialog';

const confirmationQuestion = document.createElement('p');
confirmationQuestion.id ='confirmation-question';
confirmationDialog.appendChild(confirmationQuestion);
const confirmButton = document.createElement('button');
confirmButton.id='confirm-button';
confirmationDialog.appendChild(confirmButton);

const cancelButton = document.createElement('button');
cancelButton.id='cancel-button';
confirmationDialog.appendChild(cancelButton);
document.body.appendChild(confirmationDialog);
/**
 * @param {Function} func - The function to be called.
 * @param {any} parameter - The parameter for the function.
 * @param {string} confirmQuestion - The text of the confirmation question.
 * @param {string} fontFamily - font-family
 */
function confirmWindow(func, parameter, confirmQuestion) {
    confirmationQuestion.textContent = confirmQuestion;
    confirmButton.textContent = 'Yes';
    cancelButton.textContent = 'Cancel';
    overlay.style.display = 'block';
    confirmationDialog.style.display = 'block';
    confirmButton.addEventListener('click', function () {
        closeDialogWindow();
        if (typeof func === 'function') {
            if (parameter !== undefined) {
                func(parameter);
            } else {
                func();
            }
        }
    });
}

function closeDialogWindow() {
    confirmationDialog.style.display = 'none';
    overlay.style.display = 'none';
    listFile();
}
document.addEventListener('click', function (event) {
    if (event.target === overlay || event.target === cancelButton) {
        closeDialogWindow();
    }
});
