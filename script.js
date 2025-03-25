document.getElementById("menu-toggle").addEventListener("click", function() {
    let sidebar = document.getElementById("sidebar-wrapper");
    let content = document.getElementById("page-content-wrapper");

    if (sidebar.style.display === "none") {
        sidebar.style.display = "block";
        content.style.marginLeft = "250px";
    } else {
        sidebar.style.display = "none";
        content.style.marginLeft = "0";
    }
});
