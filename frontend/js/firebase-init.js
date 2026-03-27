// firebase-init.js
// Initialize Firebase for the frontend

const firebaseConfig = {
    apiKey: "AIzaSyBfxMGcCCOhpMXVMAft6ShnfR4gPjKzDUY",
    authDomain: "ai-war-room.firebaseapp.com",
    projectId: "ai-war-room",
    storageBucket: "ai-war-room.appspot.com",
    messagingSenderId: "133507241738",
    appId: "1:133507241738:web:746028a7e029f64bf1d0c4"
};

// Initialize Firebase
console.log("firebase-init.js starting...");
if (!firebase.apps.length) {
    firebase.initializeApp(firebaseConfig);
    console.log("Firebase initialized.");
}

const auth = firebase.auth();
const googleProvider = new firebase.auth.GoogleAuthProvider();
console.log("auth and googleProvider ready. firebase object type:", typeof firebase);
