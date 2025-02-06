import { useState, useEffect } from 'react';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

export function useTelemetryWebSocket() {
  const [client, setClient] = useState(null);
  const [telemetryData, setTelemetryData] = useState(null);
  const [isConnected, setIsConnected] = useState(false);

  useEffect(() => {
    const socket = new SockJS('http://localhost:8080/ws');
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, () => {
      setIsConnected(true);
      setClient(stompClient);

      // Subscribe to physics data
      stompClient.subscribe('/topic/physics', (message) => {
        const data = JSON.parse(message.body);
        setTelemetryData(prev => ({
          ...prev,
          speedKmh: data.speedKmh,
          rpms: data.rpms,
          gear: data.gear,
          tyreTemps: {
            FL: data.tyreTempI[0],
            FR: data.tyreTempI[1],
            RL: data.tyreTempI[2],
            RR: data.tyreTempI[3]
          },
          tyrePressures: {
            FL: data.wheelsPressure[0],
            FR: data.wheelsPressure[1],
            RL: data.wheelsPressure[2],
            RR: data.wheelsPressure[3]
          }
        }));
      });

      // Subscribe to graphics data
      stompClient.subscribe('/topic/graphics', (message) => {
        const data = JSON.parse(message.body);
        setTelemetryData(prev => ({
          ...prev,
          currentLap: data.currentTime,
          lastLap: data.lastTime,
          bestLap: data.bestTime,
          position: data.position,
          completedLaps: data.completedLaps
        }));
      });
    });

    return () => {
      if (client) {
        client.disconnect();
      }
    };
  }, []);

  return { telemetryData, isConnected };
}
