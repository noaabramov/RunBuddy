const express = require('express');
const mongoose = require('mongoose');

const User = require('./users');
const Activity = require('./activities');

const dbURI = 'mongodb://127.0.0.1:27017/running_buddy_db';

const app = express();

app.use(express.json());

mongoose.connect(dbURI, {
  useNewUrlParser: true,
  useUnifiedTopology: true
}).then(() => {
  console.log('Connected to database');
}).catch((err) => {
  console.log(`Error connecting to database: ${err}`);
});

app.get('/', (req, res) => {
  const db = mongoose.connection; 
  db.on('error', console.error.bind(console, 'MongoDB connection error:'));
  db.once('open', function () {
    console.log('Connected to database!');
  });
});

//login action
app.post('/login', async(req, res) => {
  const { username, password } = req.body;
  try {
    const user = await User.findOne({ username, password });
    if (user) {
      res.json({ success: true, message: 'Login successful' }); 
    } else {
      res.json({ success: false, message: 'Incorrect username or password' });
    }
  } catch (err) {
    console.error(err);
    res.status(500).json({ success: false, message: 'Error logging in' });
  }
});

// register a new user
app.post('/register', async(req, res) => {
  const {username, password, firstName, lastName } = req.body;
  try {
    const user = new User({
      username,
      password,
      firstName,
      lastName,
    });

    await user.save();
    res.send('User created successfully');
  } catch (err) {
    console.error(err);
    res.status(500).send('Error creating user');
  }
});

// add a new activity
app.get('/newactivity/:username/:distance/:time/:longitude/:latitude', (req, res) => {
  console.log("new activity");
  // Get the username, distance, and time from the request body
  const { username, distance, time, longitude, latitude } = req.params;

  // Create a new activity instance with the required fields
  const activity = new Activity({
    username: username,
    distance: distance,
    time: time,
    location: {
      type: "Point",
      coordinates: [longitude, latitude]
    }
  });

  // Save the activity to the database
  activity.save()
    .then(() => {
      res.status(201).send('Activity created');
    })
    .catch((error) => {
      console.error(error);
      res.status(500).send('Error creating activity');
    });
});

// gets all of the activities in the user's area
app.get('/users/:username/activities/location/:longitude/:latitude/:radius', async (req, res) => {
  const {username, longitude, latitude, radius} = req.params;


  const activities = await Activity.find({
    location: {
      $near: {
        $geometry: {
          type: "Point",
          coordinates: [longitude, latitude]
        },
        $maxDistance: radius * 1000
      }
    },
    username: {
      $ne: "your_username"
    }
  });
  res.json(activities);
});

// updates user location and radius.
app.post('/location', async (req, res) => {
  try {
    const { username, location, radius } = req.body;
    const user = await User.findOneAndUpdate(username, { location, radius });
    res.status(200).send(user);
  } catch (error) {
    console.error(error);
    res.status(500).send('Server error');
  }
});

// returns user location and details
app.get('/users/:username/location', async (req, res) => {
  const username = req.params.username;
  try {
    const user = await User.findOne({ username });
    if (user) {
      const firstName = user.firstName;
      const lastName = user.lastName;
      const location = user.location;
      const radius = user.radius;

      res.json({
        firstName,
        lastName,
        location,
        radius
      });
    } else {
      res.status(404).json({ error: 'User not found' });
    }
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});


// Start the server
app.listen(3000, () => console.log(`Server started on port 3000`));
module.exports = mongoose;
