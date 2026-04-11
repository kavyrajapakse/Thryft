import { db, auth } from "./firebase.js";

import { getStorage, ref, uploadBytes, getDownloadURL }
from "https://www.gstatic.com/firebasejs/12.10.0/firebase-storage.js";

import { collection, addDoc, getDocs }
from "https://www.gstatic.com/firebasejs/12.10.0/firebase-firestore.js";

import { onAuthStateChanged }
from "https://www.gstatic.com/firebasejs/12.10.0/firebase-auth.js";

// ---------------- AUTH GUARD ----------------
onAuthStateChanged(auth, (user) => {
    if (!user) {
        window.location.href = 'index.html';
    }
});

const storage = getStorage();
const form = document.getElementById("productForm");

/* -------------------- TOAST NOTIFICATION -------------------- */
function showToast(title, message, type = 'success') {
    const toast = document.getElementById('toast');
    const toastContent = document.getElementById('toastContent');
    const toastIcon = document.getElementById('toastIcon');
    const toastTitle = document.getElementById('toastTitle');
    const toastMessage = document.getElementById('toastMessage');

    // Set icon and colors based on type
    if (type === 'success') {
        toastIcon.className = 'fas fa-check-circle text-2xl text-success';
        toastContent.style.borderLeftColor = '#16A34A';
    } else if (type === 'error') {
        toastIcon.className = 'fas fa-exclamation-circle text-2xl text-error';
        toastContent.style.borderLeftColor = '#DC2626';
    } else if (type === 'info') {
        toastIcon.className = 'fas fa-info-circle text-2xl text-info';
        toastContent.style.borderLeftColor = '#2563EB';
    }

    toastTitle.textContent = title;
    toastMessage.textContent = message;

    // Show toast
    toast.classList.remove('hidden');

    // Hide after 3 seconds
    setTimeout(() => {
        toast.classList.add('hidden');
    }, 3000);
}


/* -------------------- LOAD CATEGORIES -------------------- */

async function loadCategories() {

    const select = document.getElementById("categorySelect");

    try {

        const snapshot = await getDocs(collection(db, "categories"));

        select.innerHTML = '<option value="">Select a category</option>';

        snapshot.forEach((doc) => {

            const category = doc.data();

            const option = document.createElement("option");

            option.value = category.categoryId;
            option.textContent = category.name;

            select.appendChild(option);

        });

    } catch (error) {

        console.error("Error loading categories", error);

        select.innerHTML = '<option value="">Error loading categories</option>';

    }

}

loadCategories();


/* -------------------- ADD PRODUCT -------------------- */

form.addEventListener("submit", async (e) => {

    e.preventDefault();

    const formData = new FormData(form);

    const productId = formData.get("productId");

    const imageFiles = [
        document.getElementById("image1File").files[0],
        document.getElementById("image2File").files[0],
        document.getElementById("image3File").files[0]
    ];

    const imageUrls = [];

    try {

        /* ---------- Upload Images ---------- */

        for (let i = 0; i < imageFiles.length; i++) {

            const file = imageFiles[i];

            if (file) {

                const storageRef = ref(
                    storage,
                    `product-images/${productId}/image${i + 1}.jpg`
                );

                const snapshot = await uploadBytes(storageRef, file);

                const downloadURL = await getDownloadURL(snapshot.ref);

                imageUrls.push(downloadURL);
            }
        }

        if (imageUrls.length === 0) {
            showToast('Error!', 'Please upload at least one image', 'error');
            return;
        }

        /* ---------- Product Object ---------- */

        const product = {
            productId: productId,
            title: formData.get("title"),
            description: formData.get("description"),
            price: parseFloat(formData.get("price")),
            categoryId: formData.get("categoryId"),
            images: imageUrls,
            status: formData.get("status") === "true",
            size: formData.get("size"),
            condition: formData.get("condition"),
            color: formData.get("color"),
            createdAt: new Date()
        };

        /* ---------- Save to Firestore ---------- */

        await addDoc(collection(db, "products"), product);

        showToast('Success!', 'Product added successfully!', 'success');

        form.reset();

        // Clear image previews
        const preview1 = document.getElementById("preview1");
        const preview2 = document.getElementById("preview2");
        const preview3 = document.getElementById("preview3");

        preview1.classList.add("hidden");
        preview2.classList.add("hidden");
        preview3.classList.add("hidden");

        // Clear img src
        preview1.querySelector("img").src = "";
        preview2.querySelector("img").src = "";
        preview3.querySelector("img").src = "";

    } catch (error) {

        console.error("Error adding product:", error);

        showToast('Error!', 'Failed to add product. Please try again.', 'error');

    }

});