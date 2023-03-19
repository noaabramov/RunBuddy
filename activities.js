const mongoose = require('mongoose');

const ActivitySchema = new mongoose.Schema({
  username: {
    type: String,
    required: true,
    trim: true
  },
  distance: {
    type: Number,
    required: true
  },
  location: {
    type: {
      type: String,
      enum: ['Point'],
      required: true
    },
    coordinates: {
      type: [Number],
      required: true
    }
  },
  time: {
    type: Number,
    required: true
  },
});

ActivitySchema.index({ location: "2dsphere" });
const Activity = mongoose.model("Activity", ActivitySchema);
module.exports = Activity;
