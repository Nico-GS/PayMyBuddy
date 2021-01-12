CREATE DATABASE IF NOT EXISTS paymybuddy;
CREATE DATABASE IF NOT EXISTS paymybuddytest;
CREATE TABLE IF NOT EXISTS `paymybuddy`.`users` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `email` VARCHAR(255) NOT NULL,
  `nickname` VARCHAR(45) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `solde` DECIMAL(18,2) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `email` (`email` ASC) VISIBLE);

CREATE TABLE IF NOT EXISTS `paymybuddy`.`bank_transactions` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `iban` VARCHAR(34) NOT NULL,
  `amount` DECIMAL(18,2) NOT NULL,
  `date` DATE NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `user_id` (`user_id` ASC) VISIBLE,
  CONSTRAINT `user_id`
    FOREIGN KEY (`user_id`)
    REFERENCES `paymybuddy`.`users` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

CREATE TABLE IF NOT EXISTS `paymybuddy`.`transactions` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `sender_id` BIGINT NOT NULL,
  `receiver_id` BIGINT NOT NULL,
  `amount` DECIMAL(18,2) NOT NULL,
  `date` DATE NOT NULL,
  `description` VARCHAR(255) NULL,
  PRIMARY KEY (`id`),
  INDEX `sender_id` (`sender_id` ASC) VISIBLE,
  INDEX `receiver_id` (`receiver_id` ASC) VISIBLE,
  CONSTRAINT `sender`
    FOREIGN KEY (`sender_id`)
    REFERENCES `paymybuddy`.`users` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `receiver`
    FOREIGN KEY (`receiver_id`)
    REFERENCES `paymybuddy`.`users` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

CREATE TABLE IF NOT EXISTS `paymybuddy`.`connections`(
  `owner_id` BIGINT NOT NULL,
  `target_id` BIGINT NOT NULL,
  CONSTRAINT `owner_id`
    FOREIGN KEY (`owner_id`)
    REFERENCES `paymybuddy`.`users` (`id`)
	ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `target_id`
    FOREIGN KEY (`target_id`)
    REFERENCES `paymybuddy`.`users` (`id`)
	ON DELETE NO ACTION
    ON UPDATE NO ACTION);
CREATE TABLE IF NOT EXISTS `paymybuddytest`.`users` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `email` VARCHAR(255) NOT NULL,
  `nickname` VARCHAR(45) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `solde` DECIMAL(18,2) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `email` (`email` ASC) VISIBLE);
  
CREATE TABLE IF NOT EXISTS `paymybuddytest`.`bank_transactions` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `iban` VARCHAR(34) NOT NULL,
  `amount` DECIMAL(18,2) NOT NULL,
  `date` DATE NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `user_id` (`user_id` ASC) VISIBLE,
  CONSTRAINT `user_id`
    FOREIGN KEY (`user_id`)
    REFERENCES `paymybuddytest`.`users` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);
	
CREATE TABLE IF NOT EXISTS `paymybuddytest`.`transactions` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `sender_id` BIGINT NOT NULL,
  `receiver_id` BIGINT NOT NULL,
  `amount` DECIMAL(18,2) NOT NULL,
  `date` DATE NOT NULL,
  `description` VARCHAR(255) NULL,
  PRIMARY KEY (`id`),
  INDEX `sender_id` (`sender_id` ASC) VISIBLE,
  INDEX `receiver_id` (`receiver_id` ASC) VISIBLE,
  CONSTRAINT `sender`
    FOREIGN KEY (`sender_id`)
    REFERENCES `paymybuddytest`.`users` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `receiver`
    FOREIGN KEY (`receiver_id`)
    REFERENCES `paymybuddytest`.`users` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

CREATE TABLE IF NOT EXISTS `paymybuddytest`.`connections`(
  `owner_id` BIGINT NOT NULL,
  `target_id` BIGINT NOT NULL,
  CONSTRAINT `owner_id`
    FOREIGN KEY (`owner_id`)
    REFERENCES `paymybuddytest`.`users` (`id`)
	ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `target_id`
    FOREIGN KEY (`target_id`)
    REFERENCES `paymybuddytest`.`users` (`id`)
	ON DELETE NO ACTION
    ON UPDATE NO ACTION);