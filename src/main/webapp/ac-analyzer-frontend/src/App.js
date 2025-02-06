import React, { useState, useEffect } from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend } from 'recharts';
import { AlertCircle, Activity, Timer, Thermometer } from 'lucide-react';

export default function TelemetryDashboard() {
  const [isConnected, setIsConnected] = useState(false);
  const [selectedTrack, setSelectedTrack] = useState('');
  const [selectedCar, setSelectedCar] = useState('');
  const [tracks, setTracks] = useState([]);
  const [cars, setCars] = useState([]);
  const [telemetryData, setTelemetryData] = useState(null);
  const [lapTimes, setLapTimes] = useState([]);

  useEffect(() => {
    // Carica la lista dei tracciati disponibili
    fetch('http://localhost:8080/api/analysis/tyres/tracks')
      .then(res => res.json())
      .then(data => setTracks(data));
  }, []);

  useEffect(() => {
    if (selectedTrack) {
      // Carica le auto disponibili per il tracciato selezionato
      fetch(`http://localhost:8080/api/analysis/tyres/tracks/${selectedTrack}/cars`)
        .then(res => res.json())
        .then(data => setCars(data));
    }
  }, [selectedTrack]);

  const startTelemetry = () => {
    fetch('http://localhost:8080/api/telemetry/start', { method: 'POST' })
      .then(() => setIsConnected(true));
  };

  const stopTelemetry = () => {
    fetch('http://localhost:8080/api/telemetry/stop', { method: 'POST' })
      .then(() => setIsConnected(false));
  };

  return (
    <div className="min-h-screen bg-gray-100">
      {/* Header */}
      <div className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 py-4">
          <div className="flex justify-between items-center">
            <h1 className="text-2xl font-bold text-gray-900">AC Telemetry Dashboard</h1>
            <div className="flex items-center space-x-4">
              <select 
                className="rounded-md border-gray-300 shadow-sm"
                value={selectedTrack}
                onChange={(e) => setSelectedTrack(e.target.value)}
              >
                <option value="">Select Track</option>
                {tracks.map(track => (
                  <option key={track} value={track}>{track}</option>
                ))}
              </select>
              
              <select 
                className="rounded-md border-gray-300 shadow-sm"
                value={selectedCar}
                onChange={(e) => setSelectedCar(e.target.value)}
                disabled={!selectedTrack}
              >
                <option value="">Select Car</option>
                {cars.map(car => (
                  <option key={car} value={car}>{car}</option>
                ))}
              </select>

              <button
                onClick={isConnected ? stopTelemetry : startTelemetry}
                className={`px-4 py-2 rounded-md ${
                  isConnected 
                    ? 'bg-red-600 hover:bg-red-700 text-white'
                    : 'bg-green-600 hover:bg-green-700 text-white'
                }`}
              >
                {isConnected ? 'Stop' : 'Start'} Telemetry
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="max-w-7xl mx-auto px-4 py-6">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {/* Current Speed */}
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between">
              <div className="flex items-center">
                <Activity className="w-8 h-8 text-blue-500 mr-3" />
                <div>
                  <p className="text-sm font-medium text-gray-600">Current Speed</p>
                  <h2 className="text-2xl font-bold">{telemetryData?.speedKmh || 0} km/h</h2>
                </div>
              </div>
            </div>
          </div>

          {/* Current Lap */}
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between">
              <div className="flex items-center">
                <Timer className="w-8 h-8 text-green-500 mr-3" />
                <div>
                  <p className="text-sm font-medium text-gray-600">Current Lap</p>
                  <h2 className="text-2xl font-bold">{telemetryData?.currentLap || '--:--:---'}</h2>
                </div>
              </div>
            </div>
          </div>

          {/* Best Lap */}
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between">
              <div className="flex items-center">
                <AlertCircle className="w-8 h-8 text-purple-500 mr-3" />
                <div>
                  <p className="text-sm font-medium text-gray-600">Best Lap</p>
                  <h2 className="text-2xl font-bold">{telemetryData?.bestLap || '--:--:---'}</h2>
                </div>
              </div>
            </div>
          </div>

          {/* Tyre Temps */}
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between">
              <div className="flex items-center">
                <Thermometer className="w-8 h-8 text-red-500 mr-3" />
                <div>
                  <p className="text-sm font-medium text-gray-600">Avg Tyre Temp</p>
                  <h2 className="text-2xl font-bold">{telemetryData?.avgTyreTemp || 0}°C</h2>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Lap Times Chart */}
        <div className="mt-6 bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-medium mb-4">Lap Times</h3>
          <div className="h-64">
            <LineChart width={800} height={240} data={lapTimes}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="lapNumber" />
              <YAxis />
              <Tooltip />
              <Legend />
              <Line type="monotone" dataKey="lapTime" stroke="#8884d8" name="Lap Time" />
              <Line type="monotone" dataKey="sector1" stroke="#82ca9d" name="Sector 1" />
              <Line type="monotone" dataKey="sector2" stroke="#ffc658" name="Sector 2" />
              <Line type="monotone" dataKey="sector3" stroke="#ff7300" name="Sector 3" />
            </LineChart>
          </div>
        </div>

        {/* Tyre Data */}
        <div className="mt-6 grid grid-cols-2 gap-6">
          {/* Tyre Temperatures */}
          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-lg font-medium mb-4">Tyre Temperatures</h3>
            <div className="h-64">
              <LineChart width={400} height={240} data={telemetryData?.tyreTemps || []}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="time" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Line type="monotone" dataKey="FL" stroke="#8884d8" name="Front Left" />
                <Line type="monotone" dataKey="FR" stroke="#82ca9d" name="Front Right" />
                <Line type="monotone" dataKey="RL" stroke="#ffc658" name="Rear Left" />
                <Line type="monotone" dataKey="RR" stroke="#ff7300" name="Rear Right" />
              </LineChart>
            </div>
          </div>

          {/* Tyre Pressures */}
          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-lg font-medium mb-4">Tyre Pressures</h3>
            <div className="h-64">
              <LineChart width={400} height={240} data={telemetryData?.tyrePressures || []}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="time" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Line type="monotone" dataKey="FL" stroke="#8884d8" name="Front Left" />
                <Line type="monotone" dataKey="FR" stroke="#82ca9d" name="Front Right" />
                <Line type="monotone" dataKey="RL" stroke="#ffc658" name="Rear Left" />
                <Line type="monotone" dataKey="RR" stroke="#ff7300" name="Rear Right" />
              </LineChart>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
