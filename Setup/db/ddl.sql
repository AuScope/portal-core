DROP TABLE IF EXISTS authorities;
DROP TABLE IF EXISTS users;
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
