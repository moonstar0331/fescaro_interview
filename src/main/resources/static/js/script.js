
$('.file-input').on('change', function () {
    var fileName = $(this).val().split('\\').pop();
    $('#upload-file-name').val(fileName);
});

function down_ori(id) {
    location.href = 'http://localhost:8080/api/download/' + id + "?type=ORIGIN";
}

function down_enc(id) {
    location.href = 'http://localhost:8080/api/download/' + id + "?type=ENC";
}
