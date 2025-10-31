document.addEventListener("DOMContentLoaded", () => {
    const bottomSelect = document.getElementById("bottomSelect");
    const toppingSelect = document.getElementById("toppingSelect");
    const bottomImg = document.querySelector(".bottom-img");
    const toppingImg = document.querySelector(".topping-img");

    const PLACEHOLDER = {
        bottom: "/images/cupcakes/bottoms/bottom_placeholder.png",
        topping: "/images/cupcakes/toppings/topping_placeholder.png"
    };

    function updateImage(selectEl, imgEl, placeholder) {
        const opt = selectEl.selectedOptions[0];
        const url = opt ? opt.dataset.img : null;
        imgEl.onerror = () => {
            imgEl.onerror = null;
            imgEl.src = placeholder;
        };
        imgEl.src = url || placeholder;
    }

    function preloadFirstReal(selectEl, imgEl, placeholder) {
        const firstReal = Array.from(selectEl.options).find(o => !o.disabled && o.value);
        if (firstReal) {
            const url = firstReal.dataset.img;
            imgEl.onerror = () => {
                imgEl.onerror = null;
                imgEl.src = placeholder;
            };
            imgEl.src = url || placeholder;
        }
    }

    preloadFirstReal(bottomSelect, bottomImg, PLACEHOLDER.bottom);
    preloadFirstReal(toppingSelect, toppingImg, PLACEHOLDER.topping);

    bottomSelect.addEventListener("change", () => updateImage(bottomSelect, bottomImg, PLACEHOLDER.bottom));
    toppingSelect.addEventListener("change", () => updateImage(toppingSelect, toppingImg, PLACEHOLDER.topping));
});
