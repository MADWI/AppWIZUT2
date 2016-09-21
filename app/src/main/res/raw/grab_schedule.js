// This script is loaded into "https://edziekanat.zut.edu.pl/*"
// In WebView in WebPlanActivity
var flag = 0;
(function () {
// Are we on print table page?

if (location.pathname.indexOf("PodzGodzDruk") != -1) {
    // Pass the table to Java code
    var table = document.querySelector('table');

    // Build array of rows
    var tableDump =
        [].map.call(table.rows, function (row) {
            // Rows are arrays of cell contents
            return [].map.call(row.cells, function (cell) {
                return cell.textContent;
            });
        });

    // Pass result to Java to shouldOverrideUrlLoading
    location.href = 'js-grabbed-table:' + encodeURI(JSON.stringify(tableDump));
} else {
    // Are we on table selection page? (This page has "Semestralnie" checkbox)

    var semestralnie_checkbox = document.querySelector('input[id$="_rbJak_2"]');
    if (semestralnie_checkbox) {
        // Found checkbox
        if (!semestralnie_checkbox.checked) {
            // Not checked, click it
            semestralnie_checkbox.click();
        } else {
            var semester = document.querySelector('span[id$="_lblData"]')
            if(semester.textContent != "od: 30.09.2016 do: 26.02.2017") {
                 var next_button = document.querySelector('input[id$="_butN"]').click();
            } else {
                 window.open = function (url) {
                     location.href = url;
                  };
                 document.querySelector('input[id$="_btDrukuj"]').onclick();
            }
        }
    } else {
        var login_error = document.querySelector('.login_criteria ~ * .error_label');
        if (
            login_error && (
                login_error.textContent.indexOf("czas bezczyn") != -1 ||
                login_error.textContent.indexOf("idle timeout") != -1
            )
        ) {
            login_error.style.visibility = 'hidden';
        }
    }
}
})();
