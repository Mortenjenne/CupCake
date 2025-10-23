document.addEventListener("DOMContentLoaded", () => {
    const bottomSelect = document.getElementById("bottomSelect");
    const toppingSelect = document.getElementById("toppingSelect");
    const bottomImg = document.querySelector(".bottom-img");
    const toppingImg = document.querySelector(".topping-img");

    function updateImage(selectEl, imgEl, fallbackSrc) {
        const selected = selectEl.selectedOptions[0];
        const imgUrl = selected ? selected.dataset.img : null;
        if (imgUrl) {
            imgEl.src = imgUrl;
        } else if (fallbackSrc) {
            imgEl.src = fallbackSrc;
        }
    }

    bottomSelect.addEventListener("change", () => updateImage(bottomSelect, bottomImg));
    toppingSelect.addEventListener("change", () => updateImage(toppingSelect, toppingImg));
});
