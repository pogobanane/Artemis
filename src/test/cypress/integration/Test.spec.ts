import { artemis } from '../support/ArtemisTesting';

const loginPage = artemis.pageobjects.login;
const user = artemis.users.getStudentOne();

describe('Test change', () => {
    it('Checks if element exists', () => {
        cy.visit('/');
        loginPage.login(user);
        cy.get('#courses-register').should('exist');
    });
});
