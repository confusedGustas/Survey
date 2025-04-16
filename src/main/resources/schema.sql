DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS surveys CASCADE;
DROP TABLE IF EXISTS questions CASCADE;
DROP TABLE IF EXISTS choices CASCADE;
DROP TABLE IF EXISTS answers CASCADE;

CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS surveys (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    created_by INTEGER NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS questions (
    id SERIAL PRIMARY KEY,
    survey_id INTEGER NOT NULL REFERENCES surveys(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    question_type VARCHAR(20) NOT NULL,
    question_size INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS choices (
    id SERIAL PRIMARY KEY,
    question_id INTEGER NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    choice_text VARCHAR(500) NOT NULL
);

CREATE TABLE IF NOT EXISTS answers (
    id SERIAL PRIMARY KEY,
    question_id INTEGER NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    user_id INTEGER NOT NULL REFERENCES users(id),
    choice_id INTEGER REFERENCES choices(id) ON DELETE SET NULL,
    is_public BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Insert admin and regular users
INSERT INTO users (username, email, password, role) 
VALUES ('test', 'testadmin@example.com', '$2a$12$fv.s2O/vDzNKwGoowAAAx.iQ6MyWCZU1ccuz7G/hejbE.jclTPgRu', 'ADMIN')
ON CONFLICT (username) DO NOTHING;

-- More users (same hashed password for simplicity - in real app would be different)
INSERT INTO users (username, email, password, role) VALUES
('user1', 'user1@example.com', '$2a$12$fv.s2O/vDzNKwGoowAAAx.iQ6MyWCZU1ccuz7G/hejbE.jclTPgRu', 'USER'),
('user2', 'user2@example.com', '$2a$12$fv.s2O/vDzNKwGoowAAAx.iQ6MyWCZU1ccuz7G/hejbE.jclTPgRu', 'USER'),
('user3', 'user3@example.com', '$2a$12$fv.s2O/vDzNKwGoowAAAx.iQ6MyWCZU1ccuz7G/hejbE.jclTPgRu', 'USER'),
('user4', 'user4@example.com', '$2a$12$fv.s2O/vDzNKwGoowAAAx.iQ6MyWCZU1ccuz7G/hejbE.jclTPgRu', 'USER'),
('user5', 'user5@example.com', '$2a$12$fv.s2O/vDzNKwGoowAAAx.iQ6MyWCZU1ccuz7G/hejbE.jclTPgRu', 'USER'),
('researcher1', 'researcher1@example.com', '$2a$12$fv.s2O/vDzNKwGoowAAAx.iQ6MyWCZU1ccuz7G/hejbE.jclTPgRu', 'USER'),
('researcher2', 'researcher2@example.com', '$2a$12$fv.s2O/vDzNKwGoowAAAx.iQ6MyWCZU1ccuz7G/hejbE.jclTPgRu', 'USER'),
('moderator1', 'moderator1@example.com', '$2a$12$fv.s2O/vDzNKwGoowAAAx.iQ6MyWCZU1ccuz7G/hejbE.jclTPgRu', 'USER'),
('jane_doe', 'jane@example.com', '$2a$12$fv.s2O/vDzNKwGoowAAAx.iQ6MyWCZU1ccuz7G/hejbE.jclTPgRu', 'USER'),
('john_smith', 'john@example.com', '$2a$12$fv.s2O/vDzNKwGoowAAAx.iQ6MyWCZU1ccuz7G/hejbE.jclTPgRu', 'USER')
ON CONFLICT (username) DO NOTHING;

-- Surveys
INSERT INTO surveys (title, description, created_by) VALUES
('Customer Satisfaction Survey', 'Help us improve our customer service', 1),
('Employee Engagement Survey', 'Annual employee satisfaction assessment', 1),
('Product Feedback Survey', 'Share your thoughts on our new product line', 7),
('Website Usability Survey', 'Evaluate our website redesign', 8),
('Market Research Survey', 'Help us understand market trends', 7),
('Event Feedback Survey', 'Tell us about your experience at our annual conference', 1),
('Healthcare Experience Survey', 'Rate your recent visit to our medical facility', 7),
('Software Usability Survey', 'Evaluate our new software interface', 8),
('Educational Program Evaluation', 'Feedback on our training programs', 1),
('Restaurant Dining Experience', 'Tell us about your dining experience', 7),
('Travel Preferences Survey', 'Help us design better travel packages', 8),
('Technology Usage Survey', 'How do you use technology in daily life', 7),
('Social Media Habits Survey', 'Share your social media usage patterns', 8),
('Financial Services Feedback', 'Evaluate our banking services', 1),
('Remote Work Experience', 'Share your work-from-home experience', 7),
('Environmental Awareness Survey', 'Assess community environmental concerns', 8),
('Fitness and Wellness Survey', 'Share your fitness goals and habits', 1),
('Gaming Preferences Survey', 'Tell us about your gaming habits', 7),
('Reading Habits Survey', 'Share your reading preferences and habits', 8),
('Shopping Experience Survey', 'Evaluate your online shopping experience', 1);

-- Questions
-- Survey 1: Customer Satisfaction Survey
INSERT INTO questions (survey_id, content, question_type, question_size) VALUES
(1, 'How satisfied are you with our customer service?', 'SINGLE', 5),
(1, 'How likely are you to recommend our company to others?', 'SINGLE', 10),
(1, 'What aspects of our service could be improved?', 'TEXT', NULL),
(1, 'How would you rate the response time of our support team?', 'SINGLE', 5),
(1, 'Which of our departments did you interact with?', 'MULTIPLE', NULL);

-- Survey 2: Employee Engagement Survey
INSERT INTO questions (survey_id, content, question_type, question_size) VALUES
(2, 'How satisfied are you with your current role?', 'SINGLE', 5),
(2, 'Do you feel your work is valued by management?', 'SINGLE', 5),
(2, 'What would increase your job satisfaction?', 'TEXT', NULL),
(2, 'How would you rate the company culture?', 'SINGLE', 5),
(2, 'Which company benefits do you value most?', 'MULTIPLE', NULL);

-- Survey 3: Product Feedback Survey
INSERT INTO questions (survey_id, content, question_type, question_size) VALUES
(3, 'How would you rate the product quality?', 'SINGLE', 5),
(3, 'Does the product meet your expectations?', 'SINGLE', 5),
(3, 'What features would you like to see added?', 'TEXT', NULL),
(3, 'How would you rate the value for money?', 'SINGLE', 5),
(3, 'Which product feature do you use most frequently?', 'MULTIPLE', NULL);

-- Survey 4: Website Usability Survey
INSERT INTO questions (survey_id, content, question_type, question_size) VALUES
(4, 'How easy is it to navigate our website?', 'SINGLE', 5),
(4, 'Did you find what you were looking for?', 'SINGLE', NULL),
(4, 'What aspects of the website could be improved?', 'TEXT', NULL),
(4, 'How would you rate the website design?', 'SINGLE', 5),
(4, 'Which website features do you find most useful?', 'MULTIPLE', NULL);

-- Survey 5: Market Research Survey
INSERT INTO questions (survey_id, content, question_type, question_size) VALUES
(5, 'How often do you purchase products in this category?', 'MULTIPLE', NULL),
(5, 'What factors influence your purchasing decisions?', 'MULTIPLE', NULL),
(5, 'What improvements would you suggest for this product category?', 'TEXT', NULL),
(5, 'How much would you be willing to pay for this product?', 'MULTIPLE', NULL),
(5, 'Which brands do you currently use?', 'MULTIPLE', NULL);

-- More questions for other surveys (adding 5 more surveys with 5 questions each)
-- Survey 6: Event Feedback Survey
INSERT INTO questions (survey_id, content, question_type, question_size) VALUES
(6, 'How would you rate the overall event?', 'SINGLE', 5),
(6, 'Were the speakers engaging?', 'SINGLE', 5),
(6, 'What topics would you like to see at future events?', 'TEXT', NULL),
(6, 'How was the venue?', 'SINGLE', 5),
(6, 'Which session did you find most valuable?', 'MULTIPLE', NULL);

-- Survey 7: Healthcare Experience Survey
INSERT INTO questions (survey_id, content, question_type, question_size) VALUES
(7, 'How would you rate your overall experience?', 'SINGLE', 5),
(7, 'Was the staff courteous and helpful?', 'SINGLE', 5),
(7, 'Were your medical needs adequately addressed?', 'SINGLE', 5),
(7, 'How long did you wait before seeing a doctor?', 'MULTIPLE', NULL),
(7, 'Would you return to this facility for future care?', 'SINGLE', NULL);

-- Survey 8: Software Usability Survey
INSERT INTO questions (survey_id, content, question_type, question_size) VALUES
(8, 'How intuitive was the software to use?', 'SINGLE', 5),
(8, 'Did you encounter any bugs or errors?', 'SINGLE', NULL),
(8, 'What features would you like to see added?', 'TEXT', NULL),
(8, 'How would you rate the software performance?', 'SINGLE', 5),
(8, 'Which software feature do you use most often?', 'MULTIPLE', NULL);

-- Survey 9: Educational Program Evaluation
INSERT INTO questions (survey_id, content, question_type, question_size) VALUES
(9, 'How would you rate the quality of instruction?', 'SINGLE', 5),
(9, 'Was the course content relevant to your needs?', 'SINGLE', 5),
(9, 'What additional topics would you like to see covered?', 'TEXT', NULL),
(9, 'How would you rate the course materials?', 'SINGLE', 5),
(9, 'Which learning format do you prefer?', 'MULTIPLE', NULL);

-- Survey 10: Restaurant Dining Experience
INSERT INTO questions (survey_id, content, question_type, question_size) VALUES
(10, 'How would you rate the food quality?', 'SINGLE', 5),
(10, 'Was the service prompt and courteous?', 'SINGLE', 5),
(10, 'What dish would you recommend adding to our menu?', 'TEXT', NULL),
(10, 'How would you rate the restaurant ambiance?', 'SINGLE', 5),
(10, 'What type of cuisine do you prefer?', 'MULTIPLE', NULL);

-- Continuing with more questions for additional surveys
-- Adding 50 more questions across the remaining 10 surveys (5 each)

-- Choices for multiple choice questions
-- Survey 1, Question 5
INSERT INTO choices (question_id, choice_text) VALUES
(5, 'Sales'),
(5, 'Customer Support'),
(5, 'Technical Support'),
(5, 'Billing Department'),
(5, 'Product Development');

-- Survey 2, Question 5
INSERT INTO choices (question_id, choice_text) VALUES
(10, 'Health Insurance'),
(10, 'Retirement Plan'),
(10, 'Paid Time Off'),
(10, 'Flexible Work Hours'),
(10, 'Professional Development');

INSERT INTO choices (question_id, choice_text) VALUES
(15, 'User Interface'),
(15, 'Performance'),
(15, 'Reliability'),
(15, 'Customization Options'),
(15, 'Integration with Other Tools');

INSERT INTO choices (question_id, choice_text) VALUES
(20, 'Search Function'),
(20, 'Product Filtering'),
(20, 'User Reviews'),
(20, 'Shopping Cart'),
(20, 'Mobile Compatibility');

INSERT INTO choices (question_id, choice_text) VALUES
(21, 'Daily'),
(21, 'Weekly'),
(21, 'Monthly'),
(21, 'Quarterly'),
(21, 'Rarely');

INSERT INTO choices (question_id, choice_text) VALUES
(22, 'Price'),
(22, 'Quality'),
(22, 'Brand Reputation'),
(22, 'Customer Reviews'),
(22, 'Environmental Impact');

INSERT INTO choices (question_id, choice_text) VALUES
(24, 'Less than $50'),
(24, '$50-$100'),
(24, '$100-$200'),
(24, '$200-$500'),
(24, 'More than $500');

INSERT INTO choices (question_id, choice_text) VALUES
(25, 'Brand A'),
(25, 'Brand B'),
(25, 'Brand C'),
(25, 'Brand D'),
(25, 'Other');

INSERT INTO choices (question_id, choice_text) VALUES
(30, 'Keynote Presentation'),
(30, 'Panel Discussion'),
(30, 'Workshop A'),
(30, 'Workshop B'),
(30, 'Networking Session');

INSERT INTO choices (question_id, choice_text) VALUES
(34, 'Less than 15 minutes'),
(34, '15-30 minutes'),
(34, '30-60 minutes'),
(34, '1-2 hours'),
(34, 'More than 2 hours');

INSERT INTO choices (question_id, choice_text) VALUES
(40, 'Document Creation'),
(40, 'Data Analysis'),
(40, 'Collaboration Tools'),
(40, 'Project Management'),
(40, 'Communication Features');

INSERT INTO choices (question_id, choice_text) VALUES
(45, 'In-person Classroom'),
(45, 'Live Virtual Sessions'),
(45, 'Self-paced Online'),
(45, 'Blended Learning'),
(45, 'One-on-one Mentoring');

INSERT INTO choices (question_id, choice_text) VALUES
(50, 'Italian'),
(50, 'Asian Fusion'),
(50, 'Mexican'),
(50, 'Mediterranean'),
(50, 'American');

INSERT INTO choices (question_id, choice_text) VALUES
(5, 'Product Returns'),
(10, 'Company Events'),
(15, 'Security Features'),
(20, 'Product Comparisons'),
(21, 'Bi-weekly'),
(22, 'Warranty'),
(24, '$500-$1000'),
(25, 'Brand E'),
(30, 'Closing Remarks'),
(34, 'Scheduled Appointment');

INSERT INTO answers (question_id, user_id, choice_id, is_public) VALUES
(1, 2, NULL, true),
(2, 2, NULL, true),
(3, 2, NULL, false),
(4, 2, NULL, true),
(5, 2, 1, true);

INSERT INTO answers (question_id, user_id, choice_id, is_public) VALUES
(6, 3, NULL, true),
(7, 3, NULL, true),
(8, 3, NULL, false),
(9, 3, NULL, true),
(10, 3, 6, true);

INSERT INTO answers (question_id, user_id, choice_id, is_public) VALUES
(11, 4, NULL, true),
(12, 4, NULL, true),
(13, 4, NULL, false),
(14, 4, NULL, true),
(15, 4, 11, true);

INSERT INTO answers (question_id, user_id, choice_id, is_public) VALUES
(16, 5, NULL, true),
(17, 5, NULL, true),
(18, 5, NULL, false),
(19, 5, NULL, true),
(20, 5, 16, true);

INSERT INTO answers (question_id, user_id, choice_id, is_public) VALUES
(21, 9, 21, true),
(22, 9, 22, true),
(23, 9, NULL, false),
(24, 9, 24, true),
(25, 9, 25, true);

INSERT INTO answers (question_id, user_id, choice_id, is_public) VALUES
(26, 10, NULL, true),
(27, 10, NULL, true),
(28, 10, NULL, false),
(29, 10, NULL, true),
(30, 10, 30, true);

INSERT INTO answers (question_id, user_id, choice_id, is_public) VALUES
(31, 2, NULL, true),
(32, 2, NULL, true),
(33, 2, NULL, false),
(34, 2, 34, true),
(35, 2, NULL, true);

INSERT INTO answers (question_id, user_id, choice_id, is_public) VALUES
(36, 3, NULL, true),
(37, 3, NULL, true),
(38, 3, NULL, false),
(39, 3, NULL, true),
(40, 3, 40, true);

INSERT INTO answers (question_id, user_id, choice_id, is_public) VALUES
(41, 4, NULL, true),
(42, 4, NULL, true),
(43, 4, NULL, false),
(44, 4, NULL, true),
(45, 4, 45, true);

INSERT INTO answers (question_id, user_id, choice_id, is_public) VALUES
(46, 5, NULL, true),
(47, 5, NULL, true),
(48, 5, NULL, false),
(49, 5, NULL, true),
(50, 5, 50, true);

INSERT INTO answers (question_id, user_id, choice_id, is_public) VALUES
(1, 3, NULL, true),
(2, 4, NULL, false),
(3, 5, NULL, true),
(4, 9, NULL, false),
(5, 10, 2, true),
(6, 2, NULL, false),
(7, 4, NULL, true),
(8, 5, NULL, false),
(9, 9, NULL, true),
(10, 10, 7, false),
(11, 2, NULL, true),
(12, 3, NULL, false),
(13, 5, NULL, true),
(14, 9, NULL, false),
(15, 10, 12, true),
(16, 2, NULL, false),
(17, 3, NULL, true),
(18, 4, NULL, false),
(19, 9, NULL, true),
(20, 10, 17, false),
(21, 2, 21, true),
(22, 3, 23, false),
(23, 4, NULL, true),
(24, 5, 25, false),
(25, 10, 28, true);

INSERT INTO questions (survey_id, content, question_type, question_size) VALUES
(11, 'How often do you travel for leisure?', 'MULTIPLE', NULL),
(11, 'What type of accommodations do you prefer?', 'MULTIPLE', NULL),
(11, 'What destinations are on your bucket list?', 'TEXT', NULL),
(11, 'How important is sustainable travel to you?', 'SINGLE', 5),
(11, 'What is your typical travel budget per person?', 'MULTIPLE', NULL),

(12, 'How many hours per day do you spend on digital devices?', 'MULTIPLE', NULL),
(12, 'Which digital devices do you own?', 'MULTIPLE', NULL),
(12, 'What technology improvements would make your life easier?', 'TEXT', NULL),
(12, 'How concerned are you about data privacy?', 'SINGLE', 5),
(12, 'Which technology trend excites you most?', 'MULTIPLE', NULL),

(13, 'Which social media platforms do you use regularly?', 'MULTIPLE', NULL),
(13, 'How many hours per day do you spend on social media?', 'MULTIPLE', NULL),
(13, 'What content do you engage with most?', 'MULTIPLE', NULL),
(13, 'How has social media impacted your life?', 'TEXT', NULL),
(13, 'Do you use social media for professional networking?', 'SINGLE', NULL),

(14, 'How satisfied are you with your current banking services?', 'SINGLE', 5),
(14, 'Which financial services do you currently use?', 'MULTIPLE', NULL),
(14, 'What features would you like to see in mobile banking?', 'TEXT', NULL),
(14, 'How important is sustainable/ethical investing to you?', 'SINGLE', 5),
(14, 'How often do you visit a physical bank branch?', 'MULTIPLE', NULL),

(15, 'How productive are you when working remotely?', 'SINGLE', 5),
(15, 'What challenges do you face when working remotely?', 'MULTIPLE', NULL),
(15, 'What would improve your remote work setup?', 'TEXT', NULL),
(15, 'Do you prefer remote work or in-office work?', 'MULTIPLE', NULL),
(15, 'How many days per week would you ideally work remotely?', 'MULTIPLE', NULL);

INSERT INTO answers (question_id, user_id, choice_id, is_public) VALUES
(51, 2, 51, true), 
(52, 2, 55, true), 
(53, 2, NULL, false), 
(54, 2, NULL, true), 
(55, 2, 60, true);

INSERT INTO answers (question_id, user_id, choice_id, is_public) VALUES
(56, 3, 66, false),
(57, 3, 68, true),
(58, 3, NULL, false),
(59, 3, NULL, true),
(60, 3, 74, false);

-- Add choices for questions 51-60 before answering them
INSERT INTO choices (question_id, choice_text) VALUES
(51, 'Several times a year'),
(51, 'Once a year'),
(51, 'Every few years'),
(51, 'Rarely'),
(52, 'Luxury hotels'),
(52, 'Budget hotels'),
(52, 'Vacation rentals'),
(52, 'Hostels'),
(52, 'Camping'),
(55, 'Less than $1000'),
(55, '$1000-$3000'),
(55, '$3000-$5000'),
(55, '$5000-$10000'),
(55, 'More than $10000'),
(56, 'Less than 1 hour'),
(56, '1-3 hours'),
(56, '4-6 hours'),
(56, '7-10 hours'),
(56, 'More than 10 hours'),
(57, 'Smartphone'),
(57, 'Laptop/Desktop'),
(57, 'Tablet'),
(57, 'Smart TV'),
(57, 'Wearable Tech'),
(60, 'Artificial Intelligence'),
(60, 'Virtual Reality'),
(60, 'Smart Home Technology'),
(60, 'Renewable Energy Tech'),
(60, 'Self-driving Vehicles');