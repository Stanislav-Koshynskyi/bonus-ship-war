const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, (frame) => {
    console.log('Connected: ' + frame);
    stompClient.subscribe('/topic/test', (message) => {
        alert('Отримано: ' + message.body);
    });
    stompClient.send('/app/test', {}, 'Привіт з клієнта');
}, (error) => {
    console.error('Connection error: ', error);
});