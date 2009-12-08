DROP TABLE IF EXISTS authorities;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS jobs;
DROP TABLE IF EXISTS series;
-- --------------------------------------------------------

-- 
-- Table structure for table `users`
-- 

CREATE TABLE IF NOT EXISTS users (
  username varchar(50) NOT NULL PRIMARY KEY,
  password varchar(50) NOT NULL,
  enabled BIT NOT NULL
  );
  

-- --------------------------------------------------------

-- 
-- Table structure for table `authorities`
-- 

CREATE TABLE IF NOT EXISTS authorities (
  username varchar(50) NOT NULL,
  authority varchar(50) NOT NULL
  );


ALTER TABLE authorities ADD CONSTRAINT fk_authorities_users foreign key (username) REFERENCES users(username);

CREATE TABLE IF NOT EXISTS series (
  id integer NOT NULL PRIMARY KEY auto_increment,
  user varchar(64) NOT NULL,
  name varchar(64) NOT NULL,
  description TEXT
  );

CREATE TABLE IF NOT EXISTS jobs (
  id integer NOT NULL PRIMARY KEY auto_increment,
  series_id integer NOT NULL,
  reference varchar(128),
  name varchar(64) NOT NULL,
  description TEXT,
  scriptFile varchar(32),
  status varchar(32),
  submitDate varchar(64),
  outputDir varchar(128),
  site varchar(64),
  version varchar(32),
  numTimesteps integer,
  numParticles integer,
  numBonds integer,
  checkpointPrefix varchar(255)
  );