importScripts('https://www.gstatic.com/firebasejs/9.0.0/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/9.0.0/firebase-messaging-compat.js');

firebase.initializeApp({
  apiKey: "AIzaSyDniXBACWTBr6GzWn-kUApAxW5JA7pgUxs",
  authDomain: "bbook-73ef7.firebaseapp.com",
  projectId: "bbook-73ef7",
  storageBucket: "bbook-73ef7.firebasestorage.app",
  messagingSenderId: "698907826494",
  appId: "1:698907826494:web:e03fdce42ba65c7e82746f",
  measurementId: "G-YJJFPT71QC"
});

const messaging = firebase.messaging();

messaging.onBackgroundMessage((payload) => {
  console.log('Received background message:', payload);

  const notificationTitle = payload.notification.title;
  const notificationOptions = {
    body: payload.notification.body,
    icon: '/img/notification-icon.svg'  // SVG 아이콘 사용
  };

  self.registration.showNotification(notificationTitle, notificationOptions);
}); 